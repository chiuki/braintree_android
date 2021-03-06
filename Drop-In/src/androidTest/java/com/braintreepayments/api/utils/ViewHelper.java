package com.braintreepayments.api.utils;

import android.os.SystemClock;
import android.view.View;

import com.braintreepayments.api.dropin.R;
import com.google.android.apps.common.testing.ui.espresso.UiController;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import org.hamcrest.Matcher;

import static com.braintreepayments.api.utils.Matchers.withHint;
import static com.google.android.apps.common.testing.testrunner.util.Checks.checkNotNull;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

/* http://stackoverflow.com/a/22563297 */
public class ViewHelper {

    public static final int HALF_SECOND = 500;
    public static final int ONE_SECOND = 1000;
    public static final int TWO_SECONDS = 2000;
    public static final int THREE_SECONDS = 3000;
    public static final int FOUR_SECONDS = 4000;
    public static final int FIVE_SECONDS = 5000;
    public static final int TEN_SECONDS = 10000;

    public static boolean sWaitingForView;

    public static ViewInteraction waitForView(final Matcher<View> viewFinder, Matcher<View> viewCondition, final long millis) {
        checkNotNull(viewFinder);

        final long endTime = System.currentTimeMillis() + millis;
        ViewHelper.sWaitingForView = true;

        do {
            try {
                ViewInteraction interaction = onView(viewFinder);
                interaction.check(matches(viewCondition));

                ViewHelper.sWaitingForView = false;
                return interaction;
            } catch (Exception e) {
                // noop
            } catch (Error e) {
                // noop
            }
        } while (System.currentTimeMillis() < endTime);

        ViewHelper.sWaitingForView = false;

        ViewInteraction interaction = onView(viewFinder);
        interaction.check(matches(viewCondition));

        return interaction;
    }

    public static ViewInteraction waitForView(final Matcher<View> viewMatcher, final long millis) {
        return waitForView(viewMatcher, isDisplayed(), millis);
    }

    public static ViewInteraction waitForView(final Matcher<View> viewMatcher, Matcher<View> viewCondition) {
        return waitForView(viewMatcher, viewCondition, FOUR_SECONDS);
    }

    public static ViewInteraction waitForView(final Matcher<View> viewMatcher) {
        return waitForView(viewMatcher, FOUR_SECONDS);
    }

    public static ViewInteraction waitForPaymentMethodList() {
        return waitForView(withId(R.id.change_payment_method_link), TEN_SECONDS);
    }

    public static ViewInteraction waitForPaymentMethodList(int timeout) {
        return waitForView(withId(R.id.change_payment_method_link), timeout);
    }

    public static ViewInteraction waitForAddPaymentFormHeader() {
        return waitForView(addPaymentFormHeader(), TEN_SECONDS);
    }

    public static ViewInteraction waitForAddPaymentFormHeader(long timeout) {
        return waitForView(addPaymentFormHeader(), timeout);
    }

    public static ViewInteraction onAddPaymentFormHeader() {
        return onView(addPaymentFormHeader());
    }

    public static Matcher<View> addPaymentFormHeader() {
        return withText(R.string.form_pay_with_card_header);
    }

    public static ViewInteraction onCardField() {
        return onView(withHint("Card Number"));
    }

    public static ViewInteraction onExpirationField() {
        return onView(withHint("Expiration"));
    }

    public static ViewInteraction onCvvField() {
        return onView(withHint("CVV"));
    }

    public static ViewInteraction onPostalCodeField() {
        return onView(withHint("Postal Code"));
    }

    public static ViewAction waitForKeyboardToClose() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return anything();
            }

            @Override
            public String getDescription() {
                return "Waiting 150ms for soft keyboard to close";
            }

            @Override
            public void perform(UiController uiController, View view) {
                SystemClock.sleep(150);
            }
        };
    }
}
