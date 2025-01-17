package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class NativeCheckoutRewardViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: NativeCheckoutRewardViewHolderViewModel.ViewModel
    private val alternatePledgeButtonText = TestSubscriber.create<Int>()
    private val buttonIsEnabled = TestSubscriber<Boolean>()
    private val buttonIsGone = TestSubscriber.create<Boolean>()
    private val buttonTint = TestSubscriber.create<Int>()
    private val checkBackgroundDrawable = TestSubscriber.create<Int>()
    private val checkIsInvisible = TestSubscriber.create<Boolean>()
    private val checkTintColor = TestSubscriber.create<Int>()
    private val conversion = TestSubscriber.create<String>()
    private val conversionIsGone = TestSubscriber.create<Boolean>()
    private val description = TestSubscriber<String>()
    private val descriptionIsGone = TestSubscriber<Boolean>()
    private val endDateSectionIsGone = TestSubscriber<Boolean>()
    private val limitContainerIsGone = TestSubscriber<Boolean>()
    private val minimumAmount = TestSubscriber<String>()
    private val minimumAmountTitle = TestSubscriber<String>()
    private val remaining = TestSubscriber<String>()
    private val remainingIsGone = TestSubscriber<Boolean>()
    private val reward = TestSubscriber<Reward>()
    private val rewardItems = TestSubscriber<List<RewardsItem>>()
    private val rewardItemsAreGone = TestSubscriber<Boolean>()
    private val showPledgeFragment = TestSubscriber<Pair<Project, Reward>>()
    private val startBackingActivity = TestSubscriber<Project>()
    private val titleForNoReward = TestSubscriber<Int>()
    private val titleForReward = TestSubscriber<String?>()
    private val titleIsGone = TestSubscriber<Boolean>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = NativeCheckoutRewardViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.alternatePledgeButtonText().subscribe(this.alternatePledgeButtonText)
        this.vm.outputs.buttonIsEnabled().subscribe(this.buttonIsEnabled)
        this.vm.outputs.buttonIsGone().subscribe(this.buttonIsGone)
        this.vm.outputs.buttonTint().subscribe(this.buttonTint)
        this.vm.outputs.checkBackgroundDrawable().subscribe(this.checkBackgroundDrawable)
        this.vm.outputs.checkIsInvisible().subscribe(this.checkIsInvisible)
        this.vm.outputs.checkTintColor().subscribe(this.checkTintColor)
        this.vm.outputs.conversion().subscribe(this.conversion)
        this.vm.outputs.conversionIsGone().subscribe(this.conversionIsGone)
        this.vm.outputs.description().subscribe(this.description)
        this.vm.outputs.descriptionIsGone().subscribe(this.descriptionIsGone)
        this.vm.outputs.endDateSectionIsGone().subscribe(this.endDateSectionIsGone)
        this.vm.outputs.remaining().subscribe(this.remaining)
        this.vm.outputs.remainingIsGone().subscribe(this.remainingIsGone)
        this.vm.outputs.limitContainerIsGone().subscribe(this.limitContainerIsGone)
        this.vm.outputs.minimumAmount().map { it.toString() }.subscribe(this.minimumAmount)
        this.vm.outputs.minimumAmountTitle().map { it.toString() }.subscribe(this.minimumAmountTitle)
        this.vm.outputs.reward().subscribe(this.reward)
        this.vm.outputs.rewardItems().subscribe(this.rewardItems)
        this.vm.outputs.rewardItemsAreGone().subscribe(this.rewardItemsAreGone)
        this.vm.outputs.showPledgeFragment().subscribe(this.showPledgeFragment)
        this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity)
        this.vm.outputs.titleForNoReward().subscribe(this.titleForNoReward)
        this.vm.outputs.titleForReward().subscribe(this.titleForReward)
        this.vm.outputs.titleIsGone().subscribe(this.titleIsGone)
    }

    @Test
    fun testButtonUIOutputs() {
        setUpEnvironment(environment())
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        val noReward = RewardFactory.noReward()

        //Live project, available reward, not backed
        this.vm.inputs.projectAndReward(project, reward)
        this.buttonIsGone.assertValue(false)
        this.buttonTint.assertValue(R.color.button_pledge_live)
        this.minimumAmount.assertValuesAndClear("$20")
        this.alternatePledgeButtonText.assertNoValues()

        //Live project, no reward, not backed
        this.vm.inputs.projectAndReward(project, noReward)
        this.buttonIsGone.assertValue(false)
        this.buttonTint.assertValue(R.color.button_pledge_live)
        this.minimumAmount.assertValuesAndClear("$1")
        this.alternatePledgeButtonText.assertNoValues()

        //Live project, unavailable limited reward, not backed
        this.vm.inputs.projectAndReward(project, RewardFactory.limitReached())
        this.buttonIsGone.assertValue(false)
        this.buttonTint.assertValue(R.color.button_pledge_live)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertValue(R.string.No_longer_available)

        //Live project, unavailable expired reward, not backed
        this.vm.inputs.projectAndReward(project, RewardFactory.ended())
        this.buttonIsGone.assertValue(false)
        this.buttonTint.assertValue(R.color.button_pledge_live)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertValuesAndClear(R.string.No_longer_available)

        //Live backed project, currently backed reward
        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing()?.reward()?: RewardFactory.reward())
        this.buttonIsGone.assertValues(false)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertValuesAndClear(R.string.Manage_your_pledge)

        //Live backed project, not backed available reward
        this.vm.inputs.projectAndReward(backedLiveProject, RewardFactory.reward())
        this.buttonIsGone.assertValues(false)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage, R.color.button_pledge_live)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertValuesAndClear(R.string.Select_this_instead)

        //Live backed project, not backed unavailable limited reward
        this.vm.inputs.projectAndReward(backedLiveProject, RewardFactory.limitReached())
        this.buttonIsGone.assertValues(false)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage, R.color.button_pledge_live)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertValue(R.string.No_longer_available)

        //Live backed project, not backed unavailable expired reward
        this.vm.inputs.projectAndReward(backedLiveProject, RewardFactory.limitReached())
        this.buttonIsGone.assertValues(false)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage, R.color.button_pledge_live)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertValuesAndClear(R.string.No_longer_available)

        //Ended project, available reward, not backed
        val successfulProject = ProjectFactory.successfulProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(successfulProject, reward)
        this.buttonIsGone.assertValues(false, true)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage, R.color.button_pledge_live, R.color.button_pledge_ended)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertNoValues()

        //Ended backed project, not pledged
        val backedSuccessfulProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(backedSuccessfulProject, reward)
        this.buttonIsGone.assertValues(false, true)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage, R.color.button_pledge_live, R.color.button_pledge_ended)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertNoValues()

        //Ended backed project, no reward, not pledged
        this.vm.inputs.projectAndReward(backedSuccessfulProject, noReward)
        this.buttonIsGone.assertValues(false, true)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage, R.color.button_pledge_live, R.color.button_pledge_ended)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertNoValues()

        //Ended backed project, pledged reward
        this.vm.inputs.projectAndReward(backedSuccessfulProject, backedSuccessfulProject.backing()?.reward()?: reward)
        this.buttonIsGone.assertValues(false, true, false)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage, R.color.button_pledge_live, R.color.button_pledge_ended)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertValue(R.string.View_your_pledge)

        val backedNoRewardSuccessfulProject = ProjectFactory.backedProjectWithNoReward()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        //Ended backed project, no reward, pledged no reward
        this.vm.inputs.projectAndReward(backedNoRewardSuccessfulProject, noReward)
        this.buttonIsGone.assertValues(false, true, false)
        this.buttonTint.assertValues(R.color.button_pledge_live, R.color.button_pledge_manage, R.color.button_pledge_live, R.color.button_pledge_ended)
        this.minimumAmount.assertNoValues()
        this.alternatePledgeButtonText.assertValue(R.string.View_your_pledge)
    }

    @Test
    fun testCheckUIOutputs() {
        setUpEnvironment(environment())
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()

        this.vm.inputs.projectAndReward(project, reward)
        this.checkIsInvisible.assertValue(true)
        this.checkBackgroundDrawable.assertNoValues()
        this.checkTintColor.assertNoValues()

        this.vm.inputs.projectAndReward(project, RewardFactory.limitReached())
        this.checkIsInvisible.assertValue(true)
        this.checkBackgroundDrawable.assertNoValues()
        this.checkTintColor.assertNoValues()

        this.vm.inputs.projectAndReward(project, RewardFactory.ended())
        this.checkIsInvisible.assertValue(true)
        this.checkBackgroundDrawable.assertNoValues()
        this.checkTintColor.assertNoValues()

        this.vm.inputs.projectAndReward(project, RewardFactory.noReward())
        this.checkIsInvisible.assertValue(true)
        this.checkBackgroundDrawable.assertNoValues()
        this.checkTintColor.assertNoValues()

        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing()?.reward()?: RewardFactory.reward())
        this.checkIsInvisible.assertValues(true, false)
        this.checkBackgroundDrawable.assertValue(R.drawable.circle_blue_alpha_6)
        this.checkTintColor.assertValues(R.color.button_pledge_manage)

        this.vm.inputs.projectAndReward(backedLiveProject, RewardFactory.reward())
        this.checkIsInvisible.assertValues(true, false, true)
        this.checkBackgroundDrawable.assertValues(R.drawable.circle_blue_alpha_6)
        this.checkTintColor.assertValues(R.color.button_pledge_manage)

        val successfulProject = ProjectFactory.successfulProject()
        this.vm.inputs.projectAndReward(successfulProject, reward)
        this.checkIsInvisible.assertValues(true, false, true)
        this.checkBackgroundDrawable.assertValues(R.drawable.circle_blue_alpha_6)
        this.checkTintColor.assertValues(R.color.button_pledge_manage)

        val backedEndedProject = ProjectFactory.backedProject()
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(backedEndedProject, backedEndedProject.backing()?.reward()?: RewardFactory.reward())
        this.checkIsInvisible.assertValues(true, false, true, false)
        this.checkBackgroundDrawable.assertValues(R.drawable.circle_blue_alpha_6, R.drawable.circle_grey_300)
        this.checkTintColor.assertValues(R.color.button_pledge_manage, R.color.button_pledge_ended)
    }

    @Test
    fun testConversion() {
        setUpEnvironment(environment())
        // Set the project currency and the user's chosen currency to the same value
        val usProject = ProjectFactory.project()
        val reward = RewardFactory.reward()

        // the conversion should be hidden.
        this.vm.inputs.projectAndReward(usProject, reward)
        this.conversion.assertValueCount(1)
        this.conversionIsGone.assertValuesAndClear(true)

        val caProject = ProjectFactory.caProject().toBuilder().currentCurrency("USD").build()

        // USD conversion should shown.
        this.vm.inputs.projectAndReward(caProject, reward)
        this.conversion.assertValueCount(2)
        this.conversionIsGone.assertValues(false)
    }

    @Test
    fun testConversionTextRoundsUp_USUser_prefersUSD() {
        // Set user's country to US.
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.configForUSUser())
        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .ksCurrency(KSCurrency(currentConfig))
                .build()
        setUpEnvironment(environment)

        // Set project's country to CA with USD preference and reward minimum to $1.30.
        val project = ProjectFactory.caProject().toBuilder().currentCurrency("USD").build()
        val reward = RewardFactory.reward().toBuilder().minimum(1.3).build()

        // USD conversion should be rounded normally.
        this.vm.inputs.projectAndReward(project, reward)
        // converts to $0.98
        this.conversion.assertValuesAndClear("$1")

        this.vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().minimum(2.0).build())
        // converts to $1.50
        this.conversion.assertValue("$2")
    }

    @Test
    fun testConversionTextRoundsUp_ITUser_prefersUSD() {
        // Set user's country to IT.
        val currentConfig = MockCurrentConfig()
        currentConfig.config(ConfigFactory.configForITUser())
        val environment = environment().toBuilder()
                .currentConfig(currentConfig)
                .ksCurrency(KSCurrency(currentConfig))
                .build()
        setUpEnvironment(environment)

        // Set project's country to CA with USD preference and reward minimum to $1.30.
        val project = ProjectFactory.caProject().toBuilder().currentCurrency("USD").build()
        val reward = RewardFactory.reward().toBuilder().minimum(1.3).build()

        // USD conversion should be rounded normally.
        this.vm.inputs.projectAndReward(project, reward)
        // converts to $0.98
        this.conversion.assertValuesAndClear("US$ 1")

        this.vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().minimum(2.0).build())
        // converts to $1.50
        this.conversion.assertValue("US$ 2")
    }

    @Test
    fun testDescription() {
        setUpEnvironment(environment())

        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.description.assertValue(reward.description())
        this.descriptionIsGone.assertValue(false)

        this.vm.inputs.projectAndReward(project, RewardFactory.noReward())
        this.description.assertValues(reward.description(), null)
        this.descriptionIsGone.assertValue(false)

        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .reward(RewardFactory.noReward())
                .rewardId(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()
        this.vm.inputs.projectAndReward(backedProject, RewardFactory.noReward())
        this.description.assertValues(reward.description(), null)
        this.descriptionIsGone.assertValues(false, true)

        this.vm.inputs.projectAndReward(project, RewardFactory.noDescription())
        this.description.assertValues(reward.description(), null, "")
        this.descriptionIsGone.assertValues(false, true)
    }

    @Test
    fun testEndDateSectionIsGone() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.endDateSectionIsGone.assertValue(true)

        val expiredReward = RewardFactory.reward()
                .toBuilder()
                .endsAt(DateTime.now().minusDays(2))
                .build()

        this.vm.inputs.projectAndReward(project, expiredReward)
        this.endDateSectionIsGone.assertValue(true)

        val expiringReward = RewardFactory.reward()
                .toBuilder()
                .endsAt(DateTime.now().plusDays(2))
                .build()

        this.vm.inputs.projectAndReward(project, expiringReward)
        this.endDateSectionIsGone.assertValues(true, false)

        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), expiringReward)
        this.endDateSectionIsGone.assertValues(true, false, true)
    }

    @Test
    fun testGoToCheckoutWhenProjectIsSuccessful() {
        val project = ProjectFactory.successfulProject()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.showPledgeFragment.assertNoValues()

        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertNoValues()
    }

    @Test
    fun testGoToCheckoutWhenProjectIsSuccessfulAndHasBeenBacked() {
        val project = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        val reward = project.backing()?.reward() as Reward
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.showPledgeFragment.assertNoValues()

        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertNoValues()
    }

    @Test
    fun testGoToPledgeFragmentWhenProjectIsLive() {
        val reward = RewardFactory.reward()
        val liveProject = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(liveProject, reward)
        this.showPledgeFragment.assertNoValues()

        // When a reward from a live project is clicked, start checkout.
        this.vm.inputs.rewardClicked()
        this.showPledgeFragment.assertValue(Pair.create(liveProject, reward))
    }

    @Test
    fun testGoToViewPledge() {
        val liveProject = ProjectFactory.backedProject()
        val successfulProject = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(liveProject, liveProject.backing()?.reward() as Reward)
        this.startBackingActivity.assertNoValues()

        // When the project is still live, don't go to 'view pledge'. Should go to checkout instead.
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertNoValues()

        // When project is successful but not backed, don't go to view pledge.
        this.vm.inputs.projectAndReward(successfulProject, RewardFactory.reward())
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertNoValues()

        // When project is successful and backed, go to view pledge.
        this.vm.inputs.projectAndReward(successfulProject, successfulProject.backing()?.reward() as Reward)
        this.startBackingActivity.assertNoValues()
        this.vm.inputs.rewardClicked()
        this.startBackingActivity.assertValues(successfulProject)
    }

    @Test
    fun testButtonIsEnabled() {
        setUpEnvironment(environment())

        // A reward from a live project should be enabled.
        this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward())
        this.buttonIsEnabled.assertValue(true)

        // A reward from a successful project should not be enabled.
        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), RewardFactory.reward())
        this.buttonIsEnabled.assertValues(true, false)
        //
        // A backed reward from a live project should be enabled.
        val backedLiveProject = ProjectFactory.backedProject()
        this.vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing()?.reward()!!)
        this.buttonIsEnabled.assertValues(true, false, true)

        // A backed reward from a finished project should be enabled (distinct until changed).
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()
        this.vm.inputs.projectAndReward(backedSuccessfulProject, backedSuccessfulProject.backing()?.reward()!!)
        this.buttonIsEnabled.assertValues(true, false, true)

        // A reward with its limit reached should not be enabled.
        this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.limitReached())
        this.buttonIsEnabled.assertValues(true, false, true, false)
    }

    @Test
    fun testLimitContainerIsGone() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.limitContainerIsGone.assertValue(true)

        this.vm.inputs.projectAndReward(project, RewardFactory.limited())
        this.limitContainerIsGone.assertValues(true, false)

        this.vm.inputs.projectAndReward(project, RewardFactory.endingSoon())
        this.limitContainerIsGone.assertValues(true, false)

        val limitedExpiringReward = RewardFactory.endingSoon().toBuilder()
                .limit(10)
                .remaining(5)
                .build()
        this.vm.inputs.projectAndReward(project, limitedExpiringReward)
        this.limitContainerIsGone.assertValues(true, false)

        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), limitedExpiringReward)
        this.limitContainerIsGone.assertValues(true, false, true)
    }

    @Test
    fun testMinimumAmount_whenCAProject() {
        val project = ProjectFactory.caProject()
        val reward = RewardFactory.reward().toBuilder()
                .minimum(10.0)
                .build()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.minimumAmount.assertValue("CA$ 10")
    }

    @Test
    fun testMinimumAmountTitle() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.minimumAmountTitle.assertValue("$20")
    }

    @Test
    fun testMinimumAmountTitle_whenUKProject() {
        val project = ProjectFactory.ukProject()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.minimumAmountTitle.assertValue("£20")
    }

    @Test
    fun testRemaining() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // When reward is limited, quantity should be shown.
        this.vm.inputs.projectAndReward(project, RewardFactory.limited())
        this.remaining.assertValue("5")
        this.remainingIsGone.assertValue(false)

        // When reward's limit has been reached, don't show quantity.
        this.vm.inputs.projectAndReward(project, RewardFactory.limitReached())
        this.remainingIsGone.assertValues(false, true)

        this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), RewardFactory.limitReached())
        this.remainingIsGone.assertValues(false, true)

        // When reward has no limit, don't show quantity (distinct until changed).
        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.remainingIsGone.assertValues(false, true)
    }

    @Test
    fun testReward() {
        val project = ProjectFactory.project()
        val reward = RewardFactory.reward()
        setUpEnvironment(environment())

        this.vm.inputs.projectAndReward(project, reward)
        this.reward.assertValue(reward)
    }

    @Test
    fun testRewardItems() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Items section should be hidden when there are no items.
        this.vm.inputs.projectAndReward(project, RewardFactory.reward())
        this.rewardItemsAreGone.assertValue(true)
        this.rewardItems.assertNoValues()

        val itemizedReward = RewardFactory.itemized()
        this.vm.inputs.projectAndReward(project, itemizedReward)
        this.rewardItemsAreGone.assertValues(true, false)
        this.rewardItems.assertValues(itemizedReward.rewardsItems())
    }

    @Test
    fun testTitle() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        // Reward with no title should be hidden.
        val rewardWithNoTitle = RewardFactory.reward().toBuilder()
                .title(null)
                .build()
        this.vm.inputs.projectAndReward(project, rewardWithNoTitle)
        this.titleIsGone.assertValues(true)
        this.titleForReward.assertValuesAndClear(null)
        this.titleForNoReward.assertNoValues()

        // Reward with title should be visible.
        this.vm.inputs.projectAndReward(project, RewardFactory.noReward())
        this.titleIsGone.assertValues(true, false)
        this.titleForReward.assertNoValues()
        this.titleForNoReward.assertValuesAndClear(R.string.Make_a_pledge_without_a_reward)

        val noRewardBacking = BackingFactory.backing()
                .toBuilder()
                .reward(RewardFactory.noReward())
                .rewardId(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(noRewardBacking)
                .build()
        this.vm.inputs.projectAndReward(backedProject, RewardFactory.noReward())
        this.titleIsGone.assertValues(true, false)
        this.titleForReward.assertNoValues()
        this.titleForNoReward.assertValuesAndClear(R.string.Thank_you_for_supporting_this_project)

        val title = "Digital bundle"
        val rewardWithTitle = RewardFactory.reward().toBuilder()
                .title(title)
                .build()
        this.vm.inputs.projectAndReward(project, rewardWithTitle)
        this.titleIsGone.assertValues(true, false)
        this.titleForReward.assertValuesAndClear(title)
        this.titleForNoReward.assertNoValues()
    }
}
