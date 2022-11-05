package com.catchpig.mvvm.base.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.catchpig.mvvm.ksp.KotlinMvvmCompiler
import com.catchpig.mvvm.base.view.BaseVMView
import com.catchpig.mvvm.base.viewmodel.BaseViewModel
import com.catchpig.mvvm.widget.refresh.RefreshRecyclerView
import com.catchpig.utils.ext.logd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

/**
 * @author catchpig
 * @date 2019/4/6 11:07
 */
abstract class BaseVMActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB>(), BaseVMView {
    companion object {
        private const val TAG = "BaseVMActivity"
    }

    private val fullTag by lazy {
        "${javaClass.simpleName}_${TAG}"
    }

    val viewModel: VM by lazy {
        var type = javaClass.genericSuperclass
        var modelClass: Class<VM> = (type as ParameterizedType).actualTypeArguments[1] as Class<VM>
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[modelClass]
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initParam()
        lifecycle.addObserver(viewModel)
        initView()
        initFlow()
    }



    @Deprecated(
        "当前方法已废弃,请使用FlowExt.lifecycleRefresh()",
        replaceWith = ReplaceWith(
            expression = "flow.lifecycleRefresh(this,refresh)}"
        )
    )
    override fun <T> lifecycleFlowRefresh(
        flow: Flow<MutableList<T>>,
        refresh: RefreshRecyclerView
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            flow.flowOn(Dispatchers.IO).catch {
                refresh.updateError()
            }.onCompletion {
                "lifecycleFlowRefresh:onCompletion".logd(fullTag)
            }.collect {
                refresh.updateData(it)
            }
        }
    }

    @Deprecated(
        "当前方法已废弃,请使用FlowExt.lifecycle()",
        replaceWith = ReplaceWith(
            expression = "flow.lifecycle(this,{\n\n}){\n\n}"
        )
    )
    override fun <T> lifecycleFlow(
        flow: Flow<T>,
        errorCallback: ((t: Throwable) -> Unit)?,
        callback: T.() -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            flow.flowOn(Dispatchers.IO).catch { t: Throwable ->
                KotlinMvvmCompiler.onError(this@BaseVMActivity, t)
                errorCallback?.let {
                    errorCallback(t)
                }
            }.onCompletion {
                "lifecycleFlow:onCompletion".logd(fullTag)
            }.collect {
                callback(it)
            }
        }
    }

    @Deprecated(
        "当前方法已废弃,请使用FlowExt.lifecycleLoadingView()",
        replaceWith = ReplaceWith(
            expression = "flow.lifecycleLoadingView(this,{\n\n}){\n\n}"
        )
    )
    override fun <T> lifecycleFlowLoadingView(
        flow: Flow<T>,
        errorCallback: ((t: Throwable) -> Unit)?,
        callback: T.() -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            flow.flowOn(Dispatchers.IO).onStart {
                loadingView()
            }.onCompletion {
                "lifecycleFlowLoadingView:onCompletion".logd(fullTag)
                hideLoading()
            }.catch { t ->
                KotlinMvvmCompiler.onError(this@BaseVMActivity, t)
                errorCallback?.let {
                    errorCallback(t)
                }
            }.collect {
                callback(it)
            }
        }
    }

    @Deprecated(
        "当前方法已废弃,请使用FlowExt.lifecycleLoadingDialog()",
        replaceWith = ReplaceWith(
            expression = "flow.lifecycleLoadingDialog(this,{\n\n}){\n\n}"
        )
    )
    override fun <T> lifecycleFlowLoadingDialog(
        flow: Flow<T>,
        errorCallback: ((t: Throwable) -> Unit)?,
        callback: T.() -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            flow.flowOn(Dispatchers.IO).onStart {
                loadingDialog()
            }.onCompletion {
                "lifecycleFlowLoadingDialog:onCompletion".logd(fullTag)
                hideLoading()
            }.catch { t ->
                KotlinMvvmCompiler.onError(this@BaseVMActivity, t)
                errorCallback?.let {
                    errorCallback(t)
                }
            }.collect {
                callback(it)
            }
        }
    }
}
