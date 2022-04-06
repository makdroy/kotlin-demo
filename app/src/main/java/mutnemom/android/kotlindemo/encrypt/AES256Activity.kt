package mutnemom.android.kotlindemo.encrypt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityAes256Binding
import mutnemom.android.kotlindemo.extensions.sha256
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES256Activity : AppCompatActivity() {

    private val salt = "65cc1ae7eae5a4f6"
    private val rootKey = "s63FNLFPfcdhjWEN"
    private val contentId = "exam_hytexts_200220050202"
    private val memberId = "test89898989"

    private val keySpec = SecretKeySpec("$salt$rootKey$contentId$memberId".sha256(), "AES")
    private val iv = genIv()

    private lateinit var binding: ActivityAes256Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAes256Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }

    private fun setEvent() {
        binding.btnEncrypt.setOnClickListener { startEncryption() }
    }

    private fun startEncryption() {
        binding.editInput.text?.toString()?.let {
            encryptCbc(it.toByteArray())?.apply {
                binding.txtEncrypted.text = String(this)

                decryptCbc(this)?.apply {
                    binding.txtDecrypted.text = String(this)
                }
            }
        }
    }

    private fun decryptCbc(cipherText: ByteArray): ByteArray? = try {
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        cipher.doFinal(cipherText)
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }

    private fun encryptCbc(data: ByteArray): ByteArray? = try {
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        cipher.doFinal(data)
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }

    /* The IV is always 128 bit long */
    private fun genIv(): ByteArray {
        val result = ByteArray(128 / 8)
        SecureRandom().nextBytes(result)
        return result
    }

}
