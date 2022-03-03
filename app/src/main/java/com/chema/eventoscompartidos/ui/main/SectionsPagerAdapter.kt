package com.chema.eventoscompartidos.ui.main

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.fragment.AllUserFragment
import com.chema.eventoscompartidos.fragment.ProfileFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_events,
    R.string.tab_users
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        when(position){
            0 -> {
                Toast.makeText(context,"pagina 1",Toast.LENGTH_SHORT).show()
                return ProfileFragment()
            }
            1 -> {
                Toast.makeText(context,"pagina 2",Toast.LENGTH_SHORT).show()
                  return AllUserFragment()
            }
        }
        return PlaceholderFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}