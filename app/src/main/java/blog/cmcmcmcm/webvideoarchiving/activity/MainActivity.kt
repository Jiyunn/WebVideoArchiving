package blog.cmcmcmcm.webvideoarchiving.activity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.adapter.MainViewPagerAdapter
import blog.cmcmcmcm.webvideoarchiving.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initViewPager()
    }

    private fun initViewPager() {
        binding.tabHome.setupWithViewPager(binding.vpHome)
        binding.vpHome.adapter = MainViewPagerAdapter(supportFragmentManager, this)
    }
}
