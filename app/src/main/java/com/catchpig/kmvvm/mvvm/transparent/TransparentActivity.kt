package com.catchpig.kmvvm.mvvm.transparent

import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.catchpig.annotation.StatusBar
import com.catchpig.kmvvm.databinding.ActivityTransparentBinding
import com.catchpig.mvvm.base.activity.BaseVMActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@StatusBar(transparent = true)
class TransparentActivity : BaseVMActivity<ActivityTransparentBinding, TransparentViewModel>() {
    override fun initParam() {

    }

    override fun initView() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.banner().flowOn(Dispatchers.IO).onStart {
                loadingView()
            }.catch { t: Throwable ->
                snackBar(t.message!!)
            }.onCompletion {
                hideLoadingView()
            }.collect {
                Glide.with(this@TransparentActivity).load(it.imagePath).into(bodyBinding.banner)
                snackBar(it.title)
            }
        }
    }

    override fun initObserver() {
        viewModel.bannerLiveData.observe(this, {
            Glide.with(this).load(it.imagePath).into(bodyBinding.banner)
        })
    }
}