package dev.adds.encryptdatastore

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.adds.encryptdatastore.databinding.ActivityMainBinding
import dev.adds.encryptdatastore.preferences.user.UserStorePreference
import dev.adds.encryptdatastore.preferences.utils.AESCBCUtil
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userDataStore : UserStorePreference
    //Test without Store
    private lateinit var pair: Pair<ByteArray, ByteArray>
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userDataStore = UserStorePreference(this.dataStore)
        setListeners()

    }

    private fun setListeners() {
        with(binding){
            btnEncrypt.setOnClickListener {
                runBlocking {
                    //Test Store
                    userDataStore.updateTestDataEncrypt(tvEncrypt.text.toString())
                    //Test without Store
                    //pair = AESCBCUtil.encrypt("TEST",tvEncrypt.text.toString())
                }
            }
            btnDecrypt.setOnClickListener {
                runBlocking {
                    //Test Store
                    tvDescrypt.setText(userDataStore.dataEncrypt.map { it }.first())
                    //Test without Store
                    //tvDescrypt.setText(AESCBCUtil.decrypt("TEST",pair.first, pair.second))
                }
            }
        }
    }
}