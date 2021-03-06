package me.jbusdriver.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import jbusdriver.me.jbusdriver.R
import kotlinx.android.synthetic.main.layout_mine_collect.*
import me.jbusdriver.common.AppContext
import me.jbusdriver.common.RxBus
import me.jbusdriver.common.toast
import me.jbusdriver.mvp.MineCollectContract
import me.jbusdriver.mvp.bean.CollectErrorEvent
import me.jbusdriver.mvp.presenter.MineCollectPresenterImpl
import me.jbusdriver.ui.data.collect.ActressCollector
import me.jbusdriver.ui.data.collect.LinkCollector
import me.jbusdriver.ui.data.collect.MovieCollector

/**
 * Created by Administrator on 2017/7/17 0017.
 */
class MineCollectFragment : TabViewPagerFragment<MineCollectContract.MineCollectPresenter, MineCollectContract.MineCollectView>(), MineCollectContract.MineCollectView {
    override fun createPresenter() = MineCollectPresenterImpl()

    override val mTitles: List<String> by lazy { listOf("电影", "演员", "链接") }

    override val mFragments: List<Fragment> by lazy { listOf(MovieCollectFragment.newInstance(), ActressCollectFragment.newInstance(), LinkCollectFragment.newInstance()) }


    private val events by lazy { hashMapOf<String, String>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        RxBus.toFlowable(CollectErrorEvent::class.java).subscribeBy(onNext = {
            synchronized(events) {
                events.put(it.key, it.msg)
            }
            AppContext.instace.toast(it.msg)
        }).addTo(rxManager)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_collect, menu)
        menu?.findItem(R.id.action_collect_info)?.let { menu ->
            menu.isVisible = false
            //if (key == CollectType.Movie_Key) "电影" else if (key == CollectType.Actress_Key) "演员" else "链接"
            val key = vp_fragment?.currentItem?.let {
                if (it == 0) MovieCollector.key else if (it == 1) ActressCollector.key else LinkCollector.key
            }
            events.get(key)?.let { msg ->
                menu.isVisible = true
                menu.setOnMenuItemClickListener {
                    MaterialDialog.Builder(viewContext).content(msg).positiveText("确定").show()
                    true
                }
            }
        }
    }


    companion object {
        fun newInstance() = MineCollectFragment()
    }
}