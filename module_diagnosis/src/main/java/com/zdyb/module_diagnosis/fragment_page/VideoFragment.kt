package com.zdyb.module_diagnosis.fragment_page

import android.text.TextUtils
import android.widget.MediaController
import com.zdyb.lib_common.base.BaseNavFragment
import com.zdyb.lib_common.base.BaseViewModel
import com.zdyb.module_diagnosis.databinding.FragmentVideoBinding
import java.io.File

class VideoFragment:BaseNavFragment<FragmentVideoBinding,BaseViewModel>() {

    var isPlay = false
    lateinit var mPath :String

    companion object{

        fun instance(path:String):VideoFragment{
            val fragment = VideoFragment()
            fragment.mPath = path
            return fragment
        }
    }

    override fun initViewModel(): BaseViewModel {
        return BaseViewModel()
    }

    override fun initViewObservable() {
        super.initViewObservable()
        println("video=$mPath")

        if (!TextUtils.isEmpty(mPath)){
            val file = File(mPath)
            if (file.exists()){
                isPlay = true
                binding.videoView.setVideoPath(mPath)
                val mediaController = MediaController(requireContext())
                binding.videoView.setMediaController(mediaController)
                binding.videoView.requestFocus()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (isPlay){
            binding.videoView.start()
        }

    }

}