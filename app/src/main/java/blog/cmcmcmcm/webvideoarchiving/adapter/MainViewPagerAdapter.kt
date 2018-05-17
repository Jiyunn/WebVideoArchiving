package blog.cmcmcmcm.webvideoarchiving.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.fragment.ArchiveFragment
import blog.cmcmcmcm.webvideoarchiving.fragment.BrowserFragment

class MainViewPagerAdapter (fm : FragmentManager, val context: Context?) : FragmentPagerAdapter(fm) {

    val tabTitles = arrayOf(context?.getString(R.string.tab_browser),             context?.getString(R.string.tab_archive))

    override fun getItem(position: Int): Fragment {

        when (position) {
            0 -> return BrowserFragment.newInstance()
            1 -> return ArchiveFragment.newInstance()
        }
        return BrowserFragment.newInstance()
    }

    override fun getCount(): Int {
        return tabTitles.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabTitles[position]
    }
}