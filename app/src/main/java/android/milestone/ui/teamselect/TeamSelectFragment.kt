package android.milestone.ui.teamselect

import android.milestone.R
import android.milestone.base.BaseFragment
import android.milestone.databinding.FragmentTeamSelectBinding
import android.milestone.ui.login.viewmodel.LoginViewModel
import android.milestone.ui.teamselect.adapter.TeamSelectAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamSelectFragment : BaseFragment<FragmentTeamSelectBinding>(R.layout.fragment_team_select) {

    private val viewModel: LoginViewModel by activityViewModels()
    private val teamSelectAdapter: TeamSelectAdapter by lazy {
        TeamSelectAdapter {
            viewModel.setTeamId(it.id)
            binding.btSelect.isEnabled = true
        }
    }

    override fun initViews() {
        binding.run {
            rvTeam.adapter = teamSelectAdapter
            ivBack.setOnClickListener {
                it.findNavController().navigate(R.id.action_team_select_to_login)
            }
            tvAfterSelect.setOnClickListener {
                it.findNavController().navigate(R.id.action_team_select_to_nickname)
            }
            btSelect.setOnClickListener {
                view?.findNavController()?.navigate(R.id.action_team_select_to_nickname)
            }
        }
        initViewModels()
    }

    private fun initViewModels() {
        viewModel.run {
            teamInfo.observe(viewLifecycleOwner, {
                teamSelectAdapter.submitList(it.data)
            })
        }
    }
}