package ru.netology.tablead

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import ru.netology.tablead.accounthelper.AccountHelper
import ru.netology.tablead.act.DescriptionActivity
import ru.netology.tablead.act.EditAdsAct
import ru.netology.tablead.act.FilterActivity
import ru.netology.tablead.adapters.AdHolder
import ru.netology.tablead.adapters.AdsRcAdapter
import ru.netology.tablead.databinding.ActivityMainBinding
import ru.netology.tablead.dialoghelper.DialogConst.SIGN_IN_STATE
import ru.netology.tablead.dialoghelper.DialogConst.SIGN_UP_STATE
import ru.netology.tablead.dialoghelper.Dialoghelper
import ru.netology.tablead.model.Ad
import ru.netology.tablead.viewmodel.FireBaseViewModel

class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener, AdHolder.ItemListener {

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }

    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AdsRcAdapter
    private val dialogHelper = Dialoghelper(this)
    val myAuth = Firebase.auth
    private val fireBaseViewModel: FireBaseViewModel by viewModels()
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRcAdapter()
        initViewModel()
        buttomMenuOnClick()
        scrollListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_filter) {
            startActivity(Intent(this@MainActivity, FilterActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onActivityResult() {
        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        dialogHelper.accounthelper.signInFirebaseWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                    Log.d("MyLog", "${e.message}")
                }
            }
    }

    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad>{
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)
        if (currentCategory != getString(R.string.ad_cat)){
            tempList.clear()
            list.forEach{
                if (currentCategory == it.category)
                    tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun initViewModel() {
        fireBaseViewModel.liveAdsData.observe(this) {
            val list = it?.let { it1 -> getAdsByCategory(it1) }
            if (!clearUpdate) {
                if (it != null) {
                    adapter.updateAdapter(list!!)
                }
            } else {
                if (it != null) {
                    adapter.updateWithClear(list!!)
                    binding.mainContent.tvEmpty.visibility =
                        if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun init() {
        currentCategory = getString(R.string.ad_cat)
        setSupportActionBar(binding.mainContent.toolbar)
        onActivityResult()
        val toggel = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainContent.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggel)
        toggel.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccoutImage)
        navViewSettings()
    }

    private fun initRcAdapter() {
        binding.apply {
            adapter = AdsRcAdapter(this@MainActivity)
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    private fun buttomMenuOnClick() = with(binding) {
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            clearUpdate = true
            when (item.itemId) {
                R.id.new_ad -> {
                    val intent = Intent(this@MainActivity, EditAdsAct::class.java)
                    startActivity(intent)
                }
                R.id.id_favs -> {
                    fireBaseViewModel.loadMyFavs()
                }
                R.id.id_my_ads -> {
                    fireBaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_home -> {
                    currentCategory = getString(R.string.ad_cat)
                    fireBaseViewModel.loadAllAdsFirstPage()
                    mainContent.toolbar.title = getString(R.string.default_category)
                }
            }
            true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
        when (item.itemId) {
            R.id.my_ads -> {
                Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show()

            }
            R.id.car -> {
                getAdsFromCat(getString(R.string.ad_car))
            }
            R.id.pc -> {
                getAdsFromCat(getString(R.string.ad_pc))
            }
            R.id.smartphone -> {
                getAdsFromCat(getString(R.string.ad_smartphone))
            }
            R.id.dm -> {
                getAdsFromCat(getString(R.string.ad_dm))
            }
            R.id.sign_up -> {
                dialogHelper.createSignDialog(SIGN_UP_STATE)
            }
            R.id.sign_in -> {
                dialogHelper.createSignDialog(SIGN_IN_STATE)
            }
            R.id.sing_out -> {
                if (myAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                myAuth.signOut()
                dialogHelper.accounthelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(cat: String) {
        currentCategory = cat
        fireBaseViewModel.loadAllAdsFromCat(cat)
    }

    fun uiUpdate(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accounthelper.signInAnonimysly(object : AccountHelper.Listener {
                override fun onComplete() {
                    tvAccount.text = resources.getString(R.string.guest)
                    imAccount.setImageResource(R.drawable.ic_account)
                }
            })
        } else if (user.isAnonymous) {
            tvAccount.text = resources.getString(R.string.guest)
            imAccount.setImageResource(R.drawable.ic_account)
        } else {
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(myAuth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    override fun onDeleteItem(ad: Ad) {
        fireBaseViewModel.deleteItem(ad)
    }

    override fun onAddViewed(ad: Ad) {
        fireBaseViewModel.adViewed(ad)
        val i = Intent(this@MainActivity, DescriptionActivity::class.java)
        i.putExtra(DescriptionActivity.AD, ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        fireBaseViewModel.onFavClick(ad)
    }

    // смена цвета титла через код
    private fun navViewSettings() = with(binding) {
        val menu = navView.menu
        val adsCats = menu.findItem(R.id.adsCategory)
        val spanAdsCat = SpannableString(adsCats.title)
        adsCats.title?.let {
            spanAdsCat.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.red
                    )
                ), 0, it.length, 0
            )
        }
        adsCats.title = spanAdsCat

        val accCats = menu.findItem(R.id.accountCategory)
        val spanAcc = SpannableString(adsCats.title)
        accCats.title?.let {
            spanAcc.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        this@MainActivity,
                        R.color.red
                    )
                ), 0, it.length, 0
            )
        }
        accCats.title = spanAcc
    }

    //Отслеживаем событие  Скрола recycler view  вниз до конца direction SCROLL_DOWN = 1, если до конца вверх то direction = -1
    private fun scrollListener() = with(binding.mainContent) {
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    clearUpdate = false
                    val adsList = fireBaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()) {
                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }

    private fun getAdsFromCat(adsList: ArrayList<Ad>) {
        adsList[0].let {
            if (currentCategory == getString(R.string.ad_cat)) {
                fireBaseViewModel.loadAllAdsNextPage(it.time)
            } else {
                val catTime = "${it.category}_${it.time}"
                fireBaseViewModel.loadAllAdsFromCatNextPage(catTime)
            }
        }

    }

}