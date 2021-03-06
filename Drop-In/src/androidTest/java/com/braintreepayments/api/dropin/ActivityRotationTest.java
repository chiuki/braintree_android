package com.braintreepayments.api.dropin;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;

import com.braintreepayments.api.BraintreeApi;
import com.braintreepayments.api.TestClientTokenBuilder;
import com.braintreepayments.api.TestUtils;
import com.braintreepayments.api.exceptions.BraintreeException;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.utils.ViewHelper;

import java.util.concurrent.atomic.AtomicInteger;

import static com.braintreepayments.api.TestUtils.assertSelectedPaymentMethodIs;
import static com.braintreepayments.api.TestUtils.injectCountPaymentMethodListBraintree;
import static com.braintreepayments.api.TestUtils.injectSlowBraintree;
import static com.braintreepayments.api.TestUtils.rotateToLandscape;
import static com.braintreepayments.api.TestUtils.rotateToPortrait;
import static com.braintreepayments.api.utils.Matchers.hasBackgroundColor;
import static com.braintreepayments.api.utils.ViewHelper.onCardField;
import static com.braintreepayments.api.utils.ViewHelper.onCvvField;
import static com.braintreepayments.api.utils.ViewHelper.onExpirationField;
import static com.braintreepayments.api.utils.ViewHelper.onPostalCodeField;
import static com.braintreepayments.api.utils.ViewHelper.waitForAddPaymentFormHeader;
import static com.braintreepayments.api.utils.ViewHelper.waitForKeyboardToClose;
import static com.braintreepayments.api.utils.ViewHelper.waitForPaymentMethodList;
import static com.braintreepayments.api.utils.ViewHelper.waitForView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.closeSoftKeyboard;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isEnabled;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

public class ActivityRotationTest extends BraintreePaymentActivityTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            rotateToPortrait(this);
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            rotateToPortrait(this);
        }
    }

    public void testAddPaymentViewIsRestoredOnRotation() {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        TestUtils.setUpActivityTest(this);

        getActivity();
        waitForAddPaymentFormHeader();
        onCardField().perform(typeText("378282246310005"));
        onExpirationField().perform(typeText("12/18"), closeSoftKeyboard(),
                waitForKeyboardToClose());
        onCvvField().perform(typeText("1234"), closeSoftKeyboard(), waitForKeyboardToClose());
        onPostalCodeField().perform(typeText("12345"), closeSoftKeyboard(),
                waitForKeyboardToClose());

        rotateToLandscape(this);
        waitForAddPaymentFormHeader();
        onCardField().check(matches(withText("378282246310005")));
        onExpirationField().check(matches(withText("12/18")));
        onCvvField().check(matches(withText("1234")));
        onPostalCodeField().check(matches(withText("12345")));
        onView(withId(R.id.card_form_complete_button)).check(matches(isEnabled()));
    }

    public void testSelectPaymentViewIsRestoredOnRotation()
            throws InterruptedException, ErrorWithResponse, BraintreeException {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }
        String clientToken = TestUtils.setUpActivityTest(this);
        BraintreeApi api = new BraintreeApi(getContext(), clientToken);
        api.create(new CardBuilder()
                .cardNumber("4111111111111111")
                .expirationMonth("02")
                .expirationYear("18"));

        SystemClock.sleep(1000);

        api.create(new CardBuilder()
                .cardNumber("378282246310005")
                .expirationMonth("02")
                .expirationYear("18"));

        getActivity();

        waitForPaymentMethodList();
        onView(withId(R.id.payment_method_type)).check(matches(withText(R.string.descriptor_amex)));

        onView(withId(R.id.selected_payment_method_view)).perform(click());
        onView(withText(R.string.descriptor_visa)).perform(click());
        assertSelectedPaymentMethodIs(R.string.descriptor_visa);

        rotateToLandscape(this);
        waitForPaymentMethodList();
        assertSelectedPaymentMethodIs(R.string.descriptor_visa);
    }

    public void testDoesntReloadPaymentMethodsOnRotate() {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        final AtomicInteger listPaymentMethodsCount = new AtomicInteger(0);
        String clientToken = new TestClientTokenBuilder().build();
        injectCountPaymentMethodListBraintree(getContext(), clientToken, listPaymentMethodsCount);
        TestUtils.setUpActivityTest(this, clientToken);

        getActivity();
        waitForAddPaymentFormHeader();
        assertEquals("Callback should have been called once", 1, listPaymentMethodsCount.get());
        rotateToLandscape(this);

        waitForAddPaymentFormHeader();
        assertEquals(
                "Callback should not have been called again since the payment methods were cached.",
                1, listPaymentMethodsCount.get());
    }

    public void testWhenRotatingDeviceWhileLoadingSendsEventToNewActivity() {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        int timeout = 5000;
        String clientToken = new TestClientTokenBuilder().build();
        injectSlowBraintree(getContext(), clientToken, timeout);
        TestUtils.setUpActivityTest(this, clientToken);

        getActivity();
        rotateToLandscape(this);
        ViewHelper.waitForAddPaymentFormHeader(timeout * 2);
    }

    public void testCardFieldsStillDisabledDuringSubmitOnRotation() {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        int timeout = 4000;
        String clientToken = new TestClientTokenBuilder().build();
        injectSlowBraintree(getContext(), clientToken, timeout);
        TestUtils.setUpActivityTest(this, clientToken);

        getActivity();

        waitForAddPaymentFormHeader(timeout * 4);
        onView(withId(R.id.card_form_card_number)).perform(typeText("4111111111111111"));
        onView(withId(R.id.card_form_expiration)).perform(typeText("0119"), closeSoftKeyboard(), waitForKeyboardToClose());
        onView(withId(R.id.card_form_cvv)).perform(typeText("123"), closeSoftKeyboard(), waitForKeyboardToClose());
        onView(withId(R.id.card_form_postal_code)).perform(typeText("12345"), closeSoftKeyboard(), waitForKeyboardToClose());
        onView(withId(R.id.card_form_complete_button)).perform(click());

        onView(withId(R.id.card_form_card_number)).check(matches(not(isEnabled())));
        rotateToLandscape(this);
        onView(withId(R.id.card_form_card_number)).check(matches(not(isEnabled())));
    }

    public void testSubmitButtonIsDisabledDuringSubmitOnRotate() {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        String clientToken = new TestClientTokenBuilder().build();
        int timeout = 2000;
        injectSlowBraintree(getContext(), clientToken, timeout);
        TestUtils.setUpActivityTest(this, clientToken);

        getActivity();
        waitForAddPaymentFormHeader(timeout * 4); // give it extra time
        rotateToLandscape(this);
        onCardField().perform(typeText("378282246310005"), closeSoftKeyboard(),
                waitForKeyboardToClose());
        onExpirationField().perform(typeText("12/18"), closeSoftKeyboard(), waitForKeyboardToClose());
        onCvvField().perform(typeText("1234"), closeSoftKeyboard(), waitForKeyboardToClose());
        onPostalCodeField().perform(typeText("12345"), closeSoftKeyboard(),
                waitForKeyboardToClose());
        onView(withId(R.id.card_form_complete_button)).perform(click());
        rotateToPortrait(this);

        waitForAddPaymentFormHeader(1000);
        onView(withId(R.id.card_form_complete_button)).check(matches(not(isEnabled())));
    }

    public void testSubmittingStateIsPersistedAcrossRotations() {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        String clientToken = new TestClientTokenBuilder().build();
        int timeout = 2000;
        injectSlowBraintree(getContext(), clientToken, timeout);
        TestUtils.setUpActivityTest(this, clientToken);

        getActivity();
        waitForAddPaymentFormHeader(timeout * 4); // give it extra time
        onCardField().perform(typeText("378282246310005"), closeSoftKeyboard(),
                waitForKeyboardToClose());
        onExpirationField().perform(typeText("12/18"), closeSoftKeyboard(), waitForKeyboardToClose());
        onCvvField().perform(typeText("1234"), closeSoftKeyboard(), waitForKeyboardToClose());
        onPostalCodeField().perform(typeText("12345"), closeSoftKeyboard(),
                waitForKeyboardToClose());
        onView(withId(R.id.card_form_complete_button)).perform(click());

        waitForView(withId(R.id.header_container));

        onView(withId(R.id.header_loading_spinner)).check(matches(isDisplayed()));

        rotateToLandscape(this);
        waitForAddPaymentFormHeader(1000);

        onView(withId(R.id.card_form_complete_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.header_loading_spinner)).check(matches(isDisplayed()));
    }

    public void testSubmitButtonIsBlueAfterRotationIfFieldsAreValid() {
        if (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        TestUtils.setUpActivityTest(this);
        getActivity();
        waitForAddPaymentFormHeader();
        onCardField().perform(typeText("378282246310005"), closeSoftKeyboard(),
                waitForKeyboardToClose());
        onExpirationField().perform(typeText("12/18"), closeSoftKeyboard(), waitForKeyboardToClose());
        onCvvField().perform(typeText("1234"), closeSoftKeyboard(), waitForKeyboardToClose());
        onPostalCodeField().perform(typeText("12345"), closeSoftKeyboard(),
                waitForKeyboardToClose());


        onView(withId(R.id.card_form_complete_button)).check(
                matches(hasBackgroundColor(R.color.bt_blue)));
        rotateToLandscape(this);
        waitForAddPaymentFormHeader();
        onView(withId(R.id.card_form_complete_button)).check(
                matches(hasBackgroundColor(R.color.bt_blue)));
    }

    private Context getContext() {
        return getInstrumentation().getContext();
    }

}
