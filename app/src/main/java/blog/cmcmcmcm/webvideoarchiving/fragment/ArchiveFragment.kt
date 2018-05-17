package blog.cmcmcmcm.webvideoarchiving.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blog.cmcmcmcm.webvideoarchiving.R
import blog.cmcmcmcm.webvideoarchiving.databinding.FragmentArchiveBinding
import io.realm.Realm

class ArchiveFragment : Fragment() {

    lateinit var binding:FragmentArchiveBinding
    lateinit var realm: Realm


    companion object {
        fun newInstance() : ArchiveFragment {
            val fragment = ArchiveFragment()
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_archive, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}