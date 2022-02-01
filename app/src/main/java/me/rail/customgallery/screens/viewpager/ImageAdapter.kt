package me.rail.customgallery.screens.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import me.rail.customgallery.media.MediaStorage

class ImageAdapter(fragment: FragmentActivity): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 100

    override fun createFragment(position: Int): Fragment {

        return ImageFragment.newInstance(MediaStorage.getImageByPosition(position).uri.toString())
    }
}