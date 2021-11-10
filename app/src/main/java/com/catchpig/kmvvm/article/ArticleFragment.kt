package com.catchpig.kmvvm.article

import androidx.recyclerview.widget.LinearLayoutManager
import com.catchpig.kmvvm.R
import com.catchpig.kmvvm.adapter.ArticleAdapter
import com.catchpig.kmvvm.databinding.FragmentArticleBinding
import com.catchpig.mvvm.base.fragment.BaseVMFragment
import com.gyf.immersionbar.ktx.statusBarHeight

class ArticleFragment : BaseVMFragment<FragmentArticleBinding, ArticleViewModel>() {
    companion object {
        fun newInstance(): ArticleFragment {
            return ArticleFragment()
        }
    }

    private lateinit var articleAdapter: ArticleAdapter;

    override fun initParam() {
    }

    override fun initView() {
        getRootBanding().topView.let {
            it.setBackgroundResource(R.color.colorPrimary);
            it.post {
                it.layoutParams.height = statusBarHeight
            }
        }
        articleAdapter = ArticleAdapter(bodyBinding.refresh)
        bodyBinding.recycle.run {
            adapter = articleAdapter
            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
        }
        bodyBinding.refresh.autoRefresh()
    }

    override fun initFlow() {
        bodyBinding.refresh.setOnRefreshLoadMoreListener { nextPageIndex ->
            lifecycleFlowRefresh(viewModel.queryArticles(nextPageIndex), articleAdapter)
        }
    }

}