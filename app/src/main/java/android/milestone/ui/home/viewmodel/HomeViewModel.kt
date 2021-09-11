package android.milestone.ui.home.viewmodel

import android.milestone.base.BaseViewModel
import android.milestone.network.request.CreateReportRequest
import android.milestone.network.request.PogVoteRequest
import android.milestone.network.request.UpdateLikeRequest
import android.milestone.network.response.RootResponse
import android.milestone.network.response.home.TinderResponse
import android.milestone.network.response.home.pog_list.PogListResponse
import android.milestone.network.response.match_detail.PlayerOfGameResponse
import android.milestone.network.response.schedule.Schedule
import android.milestone.repository.home.HomeRepository
import android.milestone.ui.schedule.ui_model.ScheduleUiModel
import android.milestone.util.PrefUtil
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val homeRepository: HomeRepository
) : BaseViewModel() {

    private val _tinderResponse = MutableLiveData<TinderResponse>()
    val tinderResponse: LiveData<TinderResponse> get() = _tinderResponse

    private val _rootResponse = MutableLiveData<RootResponse>()
    val rootResponse: LiveData<RootResponse> get() = _rootResponse

    private val _currentTinderId = MutableLiveData<Int>()
    val currentTinderId: LiveData<Int> get() = _currentTinderId

    private val _reportMessage = MutableLiveData<String>()
    val reportMessage: LiveData<String> get() = _reportMessage

    private val _playerOfGameResponse = MutableLiveData<PlayerOfGameResponse>()
    val playerOfGameResponse: LiveData<PlayerOfGameResponse> get() = _playerOfGameResponse

    private val _pogListResponse = MutableLiveData<PogListResponse>()
    val pogListResponse: LiveData<PogListResponse> get() = _pogListResponse

    private val pogVoteRequestList = mutableListOf<PogVoteRequest>()

    val currentGameResponse = homeRepository.getCurrentGame().asLiveData(coroutineExceptionHandler)

    private val _scheduleData = currentGameResponse.map {
        val currentGameModel = it.data
        val schedule = currentGameModel?.run {
            Schedule(
                aTeamIcon = aTeam.icon,
                aTeamName = aTeam.name,
                aTeamScore = aTeamScore,
                bTeamIcon = bTeam.icon,
                bTeamName = bTeam.name,
                bTeamScore = bTeamScore,
                id = id,
                startTime = startTime,
                status = status
            )
        }
        schedule
    }
    val scheduleData: LiveData<ScheduleUiModel?> = _scheduleData.map { schedule ->
        schedule?.let {
            ScheduleUiModel(it)
        }
    }

    fun postPogVote(pogVoteRequestList: List<PogVoteRequest>) {
        viewModelScope.launch(coroutineExceptionHandler) {
            homeRepository.postPogVote(pogVoteRequestList)
                .collect {
                    if (it.body()?.success == true) {
                        _rootResponse.value = it.body()
                    }
                }
        }
    }

    fun getPogList() {
        viewModelScope.launch(coroutineExceptionHandler) {
            homeRepository.getPogList()
                .collect {
                    if (it.body()?.success == true) {
                        _pogListResponse.value = it.body()
                    }
                }
        }
    }

    fun getPogOfGame() {
        viewModelScope.launch(coroutineExceptionHandler) {
            homeRepository.getPogOfGame(null)
                .collect {
                    if (it.body()?.success == true) {
                        _playerOfGameResponse.value = it.body()
                    }
                }
        }
    }

    fun getTinder(count: Int = 10) {
        viewModelScope.launch(coroutineExceptionHandler) {
            homeRepository.getTinder(
                count,
                PrefUtil.getStringValue(PrefUtil.UNSELECT_TEAM_LIST, "")
            )
                .collect {
                    it.body()?.let { tinderResponse ->
                        _tinderResponse.value = tinderResponse
                    }
                }
        }
    }

    fun updateLike(updateLikeRequest: UpdateLikeRequest) {
        viewModelScope.launch(coroutineExceptionHandler) {
            homeRepository.updateLike(updateLikeRequest)
                .collect {
                    it.body()?.let {
                        // TODO: 2021-08-30 에러 및 성공 처리
                    }
                }
        }
    }

    fun createReport(createReportRequest: CreateReportRequest) {
        viewModelScope.launch(coroutineExceptionHandler) {
            homeRepository.createReport(createReportRequest)
                .collect {
                    it.body()?.let { rootResponse ->
                        // TODO: 2021-08-31 에러 및 성공 처리
                        _rootResponse.value = rootResponse
                    }
                }
        }
    }

    fun setCurrentTinderId(tinderId: Int) {
        _currentTinderId.value = tinderId
    }

    fun setReportMessage(msg: String) {
        _reportMessage.value = msg
    }

    fun setPogVoteCount(gamePlayerId: Int) {
        val pogVoteRequest = pogVoteRequestList.find {
            it.gamePlayerId == gamePlayerId
        }
        if (pogVoteRequest == null) {
            pogVoteRequestList.add(PogVoteRequest(gamePlayerId, 1))
        } else {
            val index = pogVoteRequestList.indexOf(pogVoteRequest)
            pogVoteRequestList[index] = pogVoteRequest.copy(count = pogVoteRequest.count + 1)
        }
    }

}