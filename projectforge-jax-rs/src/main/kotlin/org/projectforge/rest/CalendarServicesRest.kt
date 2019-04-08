package org.projectforge.rest

import org.projectforge.business.teamcal.filter.CalendarFilter
import org.projectforge.business.teamcal.filter.ViewType
import org.projectforge.business.timesheet.OrderDirection
import org.projectforge.business.timesheet.TimesheetDao
import org.projectforge.business.timesheet.TimesheetFilter
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.framework.time.DateHelper
import org.projectforge.rest.core.RestHelper
import org.projectforge.rest.core.RestUserPreferencesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * For uploading address immages.
 */
@Component
@Path("calendar")
class CalendarServicesRest() {

    internal class CalendarData(val date : Date, val viewType : ViewType, val event : List<BigCalendarEvent>)
    internal class BigCalendarEvent(val id: Int, val title: String, val start: Date, val end: Date, val allDay: Boolean = false, val desc: String? = null)

    private val log = org.slf4j.LoggerFactory.getLogger(CalendarServicesRest::class.java)

    companion object {
        val USERPREF_KEY = "CalendarPage.userPrefs";
    }

    @Autowired
    private lateinit var timesheetDao: TimesheetDao

    @Autowired
    private lateinit var restUserPreferencesService: RestUserPreferencesService

    private val restHelper = RestHelper()

    @GET
    @Path("initial")
    @Produces(MediaType.APPLICATION_JSON)
    fun getInitialCalendar(@Context request: HttpServletRequest): Response {
///        val userId = requestedUserId ?: ThreadLocalUserContext.getUserId()
        var filter: CalendarFilter? = restUserPreferencesService.getEntry(request, USERPREF_KEY) as CalendarFilter
        if (filter == null) {
            filter = CalendarFilter()
            restUserPreferencesService.putEntry(request, USERPREF_KEY, filter, true)
        }
        val events = mutableListOf<BigCalendarEvent>()
        if (filter.isShowTimesheets) {
            val tsFilter = TimesheetFilter()
            when (filter.viewType) {
                ViewType.MONTH -> {
                    tsFilter.startTime = filter.startDate.withDayOfMonth(1).toDate()
                    tsFilter.stopTime = filter.startDate.dayOfMonth().withMaximumValue().toDate()
                }
                ViewType.BASIC_WEEK -> {
                    tsFilter.startTime = filter.startDate.withDayOfWeek(1).toDate()
                    tsFilter.stopTime = filter.startDate.dayOfWeek().withMaximumValue().toDate()
                }
                ViewType.BASIC_DAY -> {
                    tsFilter.startTime = filter.startDate.toDate()
                    tsFilter.stopTime = filter.startDate.plusDays(1).toDate()
                }
            }
            val timesheets = timesheetDao.getList(tsFilter)
            timesheets.forEach {
                events.add(BigCalendarEvent(it.id, it.description, it.startTime, it.stopTime))
            }
        }
        return restHelper.buildResponse(events)
    }

    @GET
    @Path("eventList")
    @Produces(MediaType.APPLICATION_JSON)
    fun getEventList(@Context request: HttpServletRequest, @QueryParam("userId") requestedUserId: Int?): Response {
/*        val userId = requestedUserId ?: ThreadLocalUserContext.getUserId()
        var filter: CalendarFilter? = restUserPreferencesService.getEntry(request, USERPREF_KEY) as CalendarFilter
        if (filter == null) {
            filter = CalendarFilter()
            restUserPreferencesService.putEntry(request, USERPREF_KEY, filter, true)
        }*/
        val tsFilter = TimesheetFilter()
        tsFilter.userId = 2
        tsFilter.startTime = DateHelper.parseIsoDate("2019-02-01 00:00:00", ThreadLocalUserContext.getTimeZone())
        tsFilter.stopTime = DateHelper.parseIsoDate("2019-03-01 00:00:00", ThreadLocalUserContext.getTimeZone())
        //tsFilter.startTime = start.toDate()
        //tsFilter.stopTime = end.toDate()
        tsFilter.orderType = OrderDirection.ASC
        val timesheets = timesheetDao.getList(tsFilter)
        val events = mutableListOf<CalendarServicesRest.BigCalendarEvent>()

        timesheets.forEach {
            events.add(CalendarServicesRest.BigCalendarEvent(it.id, it.description, it.startTime, it.stopTime))
        }

        //timesheetDao.get
        //return restHelper.buildResponse(AbstractStandardRest.InitialListData(ui = layout, data = resultSet, filter = filter))
        return Response.ok().build()
    }

}
