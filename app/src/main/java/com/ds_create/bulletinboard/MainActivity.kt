package com.ds_create.bulletinboard

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
import com.ds_create.bulletinboard.accounthelper.AccountHelper
import com.ds_create.bulletinboard.act.DescriptionActivity
import com.ds_create.bulletinboard.act.EditAdsAct
import com.ds_create.bulletinboard.act.FilterActivity
import com.ds_create.bulletinboard.adapters.AdsRcAdapter
import com.ds_create.bulletinboard.databinding.ActivityMainBinding
import com.ds_create.bulletinboard.dialoghelper.DialogConst
import com.ds_create.bulletinboard.dialoghelper.DialogHelper
import com.ds_create.bulletinboard.model.Ad
import com.ds_create.bulletinboard.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {

    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.id_filter)
            startActivity(Intent(this@MainActivity, FilterActivity::class.java))
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    private fun onActivityResult() {
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    Log.d("MyLog", "Api 0")
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("MyLog", "Api error: ${e.message}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this, {
            val list = getAdsByCategory(it)
            if (!clearUpdate) {
                adapter.updateAdapter(list)
            } else {
                adapter.updateWithClear(list)
            }
            binding.mainContent.tvEmpty.visibility = if (adapter.itemCount == 0)
                View.VISIBLE else View.GONE
        })
    }

    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad> {
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)
        if (currentCategory != getString(R.string.def)) {
            tempList.clear()
            list.forEach {
                if (currentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

     private fun init() {
         currentCategory = getString(R.string.def)
        setSupportActionBar(binding.mainContent.toolbar)
        onActivityResult()
        navViewSettings()
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.mainContent.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccountImage)
    }

    private fun bottomMenuOnClick() = with(binding){
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            clearUpdate = true
            when(item.itemId) {
                R.id.id_new_ad -> {
                    val i = Intent(this@MainActivity, EditAdsAct::class.java)
                    startActivity(i)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.add_my_ads)
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                }
                R.id.id_home -> {
                    currentCategory = getString(R.string.def)
                    firebaseViewModel.loadAllAdsFirstPage()
                    mainContent.toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
        when (item.itemId) {
            R.id.id_my_ads -> {
                Toast.makeText(this, "Pressed id_my_ads", Toast.LENGTH_LONG).show()
            }
            R.id.id_car -> {
               getAdsFromCat(getString(R.string.add_car))
            }
            R.id.id_pc -> {
                getAdsFromCat(getString(R.string.add_pc))
            }
            R.id.id_smart -> {
                getAdsFromCat(getString(R.string.add_smartphone))
            }
            R.id.id_dm -> {
                getAdsFromCat(getString(R.string.add_dm))
            }
            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }
            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }
            R.id.id_sign_out -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutG()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(cat: String) {
        currentCategory = cat
        firebaseViewModel.loadAllAdsFromCat(cat)
    }

    fun uiUpdate(user:FirebaseUser?) {

       if (user == null) {
           dialogHelper.accHelper.signInAnonymously(object: AccountHelper.Listener {
               override fun onComplete() {
                   tvAccount.setText(R.string.guest)
                   imAccount.setImageResource(R.drawable.ic_account_def)
               }
           })
        } else if (user.isAnonymous) {
            tvAccount.setText(R.string.guest)
           imAccount.setImageResource(R.drawable.ic_account_def)
        } else if (!user.isAnonymous) {
            tvAccount.text = user.email
           Picasso.get().load(user.photoUrl).into(imAccount)
       }
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAddViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra(DescriptionActivity.AD, ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSettings() = with(binding) {
        val menu = navView.menu
        val adsCat = menu.findItem(R.id.ads_cat)
        val spanAdsCat = SpannableString(adsCat.title)
        spanAdsCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity,
            R.color.color_red)), 0, adsCat.title.length, 0)
        adsCat.title = spanAdsCat

        val accCat = menu.findItem(R.id.acc_cat)
        val spanAccCat = SpannableString(accCat.title)
        spanAccCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity,
        R.color.color_red)), 0, accCat.title.length, 0)
        accCat.title = spanAccCat
    }

    private fun scrollListener() = with(binding.mainContent) {
        rcView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recView, newState)
                if (!recView.canScrollVertically(SCROLL_DOWN)
                    && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        clearUpdate = false
                    val adsList = firebaseViewModel.liveAdsData.value!!
                    if (adsList.isNotEmpty()) {
                        getAdsFromCat(adsList)
                    }
                }
            }
        })
    }

    private fun  getAdsFromCat(adsList: ArrayList<Ad>) {
        adsList[0].let {
            if (currentCategory == getString(R.string.def)) {
                firebaseViewModel.loadAllAdsNextPage(it.time)
            } else {
                val catTime = "${it.category}_${it.time}"
                    firebaseViewModel.loadAllAdsFromCatNextPage(catTime)
            }
        }
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }
}