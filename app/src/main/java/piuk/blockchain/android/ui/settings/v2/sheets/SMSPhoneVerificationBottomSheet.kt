package piuk.blockchain.android.ui.settings.v2.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.blockchain.commonarch.presentation.base.HostedBottomSheet
import com.blockchain.commonarch.presentation.mvi.MviBottomSheet
import com.blockchain.koin.scopedInject
import piuk.blockchain.android.R
import piuk.blockchain.android.databinding.BottomSheetCodeSmsVerificationBinding
import piuk.blockchain.android.ui.customviews.ToastCustom
import piuk.blockchain.android.ui.customviews.toast

class SMSPhoneVerificationBottomSheet :
    MviBottomSheet<SMSVerificationModel, SMSVerificationIntent,
        SMSVerificationState, BottomSheetCodeSmsVerificationBinding>() {

    interface Host : HostedBottomSheet.Host {
        fun onPhoneNumberVerified()
    }

    override val model: SMSVerificationModel by scopedInject()

    override val host: Host by lazy {
        super.host as? Host ?: throw IllegalStateException(
            "Host fragment is not a CodeSMSVerificationBottomSheet.Host"
        )
    }

    private val phoneNumber: String by lazy {
        arguments?.getString(PHONE_NUMBER).orEmpty()
    }

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetCodeSmsVerificationBinding =
        BottomSheetCodeSmsVerificationBinding.inflate(inflater, container, false)

    override fun initControls(binding: BottomSheetCodeSmsVerificationBinding) {
        setupUI()
    }

    override fun render(newState: SMSVerificationState) {
        when (newState.error) {
            VerificationError.VerifyPhoneError -> toast(
                getString(R.string.profile_verification_code_error), ToastCustom.TYPE_ERROR
            )
            VerificationError.ResendSmsError -> toast(
                getString(R.string.profile_resend_sms_error), ToastCustom.TYPE_ERROR
            )
        }
        if (newState.isCodeSmsSent) {
            toast(getString(R.string.code_verification_resent_sms), ToastCustom.TYPE_OK)
            model.process(SMSVerificationIntent.ResetCodeSentVerification)
        }
        if (newState.isPhoneVerified) {
            toast(getString(R.string.sms_verified), ToastCustom.TYPE_OK)
            host.onPhoneNumberVerified()
            dismiss()
        }
    }

    private fun setupUI() {
        with(binding) {
            smsCode.apply {
                singleLine = true
                labelText = getString(R.string.code_verification_enter)
                onValueChange = { value = it }
                placeholderText = context.getString(R.string.code_verification_placeholder)
            }
            resendSms.apply {
                text = getString(R.string.code_verification_re_send_text)
                onClick = { resendSMS() }
            }
            verifyCode.apply {
                text = getString(R.string.code_verification_verify_code)
                onClick = { verifyCode() }
            }
            sheetHeader.apply {
                title = getString(R.string.code_verification_title)
                onClosePress = {
                    this@SMSPhoneVerificationBottomSheet.dismiss()
                }
            }
        }
    }

    private fun resendSMS() {
        model.process(SMSVerificationIntent.ResendSMS(phoneNumber))
    }

    private fun verifyCode() {
        model.process(SMSVerificationIntent.VerifySMSCode(binding.smsCode.value))
    }

    companion object {
        private const val PHONE_NUMBER = "phone_number"

        fun newInstance(phoneNumber: String) =
            SMSPhoneVerificationBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(PHONE_NUMBER, phoneNumber)
                }
            }
    }
}
