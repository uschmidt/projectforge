import moment from 'moment-timezone';

import 'moment/min/locales';
import PropTypes from 'prop-types';
import React from 'react';
import { Calendar, momentLocalizer } from 'react-big-calendar';
import withDragAndDrop from 'react-big-calendar/lib/addons/dragAndDrop';

import 'react-big-calendar/lib/addons/dragAndDrop/styles.css';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import { connect } from 'react-redux';
import { Route } from 'react-router-dom';
import LoadingContainer from '../../../components/design/loading-container';
import history from '../../../utilities/history';
import { fetchJsonGet, fetchJsonPost } from '../../../utilities/rest';
import EditModal from '../../page/edit/EditModal';
import {
    dayStyle,
    renderAgendaEvent,
    renderDateHeader,
    renderEvent,
    renderMonthEvent,
} from './CalendarRendering';
import CalendarToolBar from './CalendarToolBar';

const localizer = momentLocalizer(moment);

const DragAndDropCalendar = withDragAndDrop(Calendar);

const convertJsonDates = e => Object.assign({}, e, {
    start: new Date(e.start),
    end: new Date(e.end),
});


class CalendarPanel extends React.Component {
    constructor(props) {
        super(props);

        const { firstDayOfWeek, timeZone, locale } = this.props;
        const useLocale = locale || 'en';
        moment.tz.setDefault(timeZone);
        moment.updateLocale(useLocale,
            {
                week: {
                    dow: firstDayOfWeek, // First day of week (got from UserStatus).
                    doy: 1, // First day of year (not yet supported).
                },
            });

        const { defaultDate, defaultView } = props;

        this.state = {
            loading: false,
            events: undefined,
            specialDays: undefined,
            date: defaultDate,
            view: defaultView,
            start: defaultDate,
            end: undefined,
            calendar: '',
        };

        this.eventStyle = this.eventStyle.bind(this);
        this.navigateToDay = this.navigateToDay.bind(this);
        this.fetchEvents = this.fetchEvents.bind(this);
        this.onRangeChange = this.onRangeChange.bind(this);
        this.onSelectSlot = this.onSelectSlot.bind(this);
        this.onSelectEvent = this.onSelectEvent.bind(this);
        this.onNavigate = this.onNavigate.bind(this);
        this.onView = this.onView.bind(this);
        this.onEventResize = this.onEventResize.bind(this);
        this.onEventDrop = this.onEventDrop.bind(this);
    }

    componentDidMount() {
        this.fetchEvents();
    }

    componentWillReceiveProps({ location: nextLocation }) {
        const { location } = this.props;

        if (
            nextLocation.state !== location.state
            && nextLocation.state
            && nextLocation.state.date
        ) {
            this.setState({
                date: new Date(nextLocation.state.date),
            });
        }
    }

    componentDidUpdate(
        {
            activeCalendars: prevActiveCalendars,
            timesheetUserId: prevTimesheetUserId,
        },
    ) {
        const { activeCalendars, timesheetUserId } = this.props;

        if (timesheetUserId !== prevTimesheetUserId) {
            this.fetchEvents();
            return;
        }

        if (prevActiveCalendars === activeCalendars || activeCalendars == null) {
            return;
        }

        if (
            prevActiveCalendars.length === activeCalendars.length
            && prevActiveCalendars.find((preActiveElement, i) => {
                const activeElement = activeCalendars[i];
                const preColor = preActiveElement.style
                    ? preActiveElement.style.bgColor : undefined;
                const activeColor = activeElement.style
                    ? activeElement.style.bgColor : undefined;

                return !(preActiveElement.id === activeElement.id
                    && preActiveElement.visible === activeElement.visible
                    && preColor === activeColor);
            }) === undefined
        ) {
            return;
        }

        this.fetchEvents();
    }

    // ToDo
    // DateHeader for statistics.

    onNavigate(date) {
        this.setState({ date });
    }

    onView(view) {
        this.setState({ view });
    }

    // Callback fired when the visible date range changes. Returns an Array of dates or an object
    // with start and end dates for BUILTIN views.
    onRangeChange(event, newView) {
        const { view } = this.state;
        let useView = newView;
        if (newView) {
            this.setState({ view: newView });
        } else {
            // newView isn't given (view not changed), so get view from state:
            useView = view;
        }
        const { start, end } = event;
        let newStart;
        let newEnd;
        if (useView === 'month' || useView === 'agenda') {
            newStart = start;
            newEnd = end;
        } else {
            const [element] = event;
            newStart = element;
        }
        this.setState({
            start: newStart,
            end: newEnd,
            view: useView,
        }, () => this.fetchEvents());
    }

    // Callback fired when a calendar event is selected.
    onSelectEvent(event) {
        const { match } = this.props;
        // start date is send to the server and is needed for series events to detect the current
        // selected event of a series.
        history.push(`${match.url}/${event.category}/edit/${event.uid || event.dbId}?startDate=${event.start.getTime() / 1000}&endDate=${event.end.getTime() / 1000}`);
    }

    // A callback fired when a date selection is made. Only fires when selectable is true.
    onSelectSlot(info) {
        const { calendar } = this.state;
        this.fetchAction('slotSelected', info.start, info.end, calendar);
    }

    onEventResize(info) {
        this.fetchAction('resize', info.start, info.end, undefined, info.allDay, info.event.category, info.event.dbId, info.event.uid);
    }

    onEventDrop(info) {
        this.fetchAction('dragAndDrop', info.start, info.end, undefined, info.allDay, info.event.category, info.event.dbId, info.event.uid);
    }

    fetchAction(action, start, end, calendar, allDay, category, dbId, uid) {
        const { match } = this.props;

        fetchJsonGet('calendar/action',
            {
                action,
                start: start ? start.toJSON() : '',
                end: end ? end.toJSON() : '',
                allDay,
                category: category || '',
                dbId: dbId || '',
                uid: uid || '',
            },
            (json) => {
                const { variables } = json;
                history.push(`${match.url}/${variables.url}`);
            });
    }

    eventStyle(event) {
        const { viewType } = this.state;
        if (viewType === 'agenda') {
            return { // Don't change style for agenda:
                className: '',
            };
        }
        // Event is always undefined!!!
        const backgroundColor = (event && event.bgColor) ? event.bgColor : undefined;
        const textColor = (event && event.fgColor) ? event.fgColor : undefined;
        const cssClass = (event && event.cssClass) ? event.cssClass : undefined;
        return {
            style: {
                backgroundColor,
                color: textColor,
            },
            className: cssClass,
        };
    }

    navigateToDay(e) {
        console.log('*** ToDo: navigate to day.', e);
        this.setState({
            date: e,
            viewType: 'day',
        });
    }

    fetchEvents() {
        const { start, end, view } = this.state;
        const { activeCalendars, timesheetUserId } = this.props;
        const activeCalendarIds = activeCalendars ? activeCalendars.map(obj => obj.id) : [];
        this.setState({ loading: true });
        fetchJsonPost('calendar/events',
            {
                start,
                end,
                view,
                activeCalendarIds,
                timesheetUserId,
                updateState: true,
                useVisibilityState: true,
                // Needed as a workaround if the user's timezone (backend) differs from timezone of
                // the browser. BigCalendar doesn't use moment's timezone for converting the
                // dates start and end. They will be converted by using the browser's timezone.
                // With this timeZone, the server is able to detect the correct start-end
                // interval of the requested events.
                timeZone: Intl.DateTimeFormat()
                    .resolvedOptions().timeZone,
            },
            (json) => {
                const { events, specialDays } = json;
                this.setState(
                    {
                        loading: false,
                        events: events.map(convertJsonDates),
                        specialDays,
                    },
                );
            },
        );
    }

    render() {
        const { events, loading } = this.state;
        if (!events) {
            return (
                <LoadingContainer loading={loading}>
                    ...
                </LoadingContainer>
            );
        }
        const {
            date,
            view,
            specialDays,
        } = this.state;
        const { topHeight, translations, match } = this.props;
        const initTime = new Date(date.getDate());
        initTime.setHours(8);
        initTime.setMinutes(0);

        return (
            <LoadingContainer loading={loading}>
                <DragAndDropCalendar
                    style={{
                        minHeight: 500,
                        height: `calc(100vh - ${topHeight})`,
                    }}
                    localizer={localizer}
                    events={events}
                    step={30}
                    view={view}
                    onView={this.onView}
                    views={['month', 'work_week', 'week', 'day', 'agenda']}
                    startAccessor="start"
                    date={date}
                    onNavigate={this.onNavigate}
                    endAccessor="end"
                    onRangeChange={this.onRangeChange}
                    onSelectEvent={this.onSelectEvent}
                    onSelectSlot={this.onSelectSlot}
                    onEventResize={this.onEventResize}
                    onEventDrop={this.onEventDrop}
                    selectable
                    eventPropGetter={this.eventStyle}
                    dayPropGetter={day => dayStyle(day, specialDays)}
                    showMultiDayTimes
                    timeslots={1}
                    scrollToTime={initTime}
                    components={{
                        event: renderEvent,
                        month: {
                            event: renderMonthEvent,
                            dateHeader: entry => renderDateHeader(entry,
                                specialDays,
                                this.navigateToDay),
                        },
                        week: {
                            // header: renderDateHeader
                        },
                        agenda: {
                            event: renderAgendaEvent,
                        },
                        toolbar: CalendarToolBar,
                    }}
                    messages={translations}
                />
                <Route
                    path={`${match.url}/:category/edit/:id?`}
                    render={props => <EditModal baseUrl={match.url} {...props} />}
                />
            </LoadingContainer>
        );
    }
}

CalendarPanel.propTypes = {
    activeCalendars: PropTypes.arrayOf(PropTypes.shape({})),
    timesheetUserId: PropTypes.number,
    firstDayOfWeek: PropTypes.number.isRequired,
    timeZone: PropTypes.string.isRequired,
    locale: PropTypes.string,
    topHeight: PropTypes.string,
    defaultDate: PropTypes.instanceOf(Date),
    defaultView: PropTypes.string,
    translations: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({
        url: PropTypes.string.isRequired,
    }).isRequired,
    location: PropTypes.shape({}).isRequired,
};

CalendarPanel.defaultProps = {
    activeCalendars: [],
    timesheetUserId: undefined,
    locale: undefined,
    topHeight: '164px',
    defaultDate: new Date(),
    defaultView: 'month',
};

const mapStateToProps = ({ authentication }) => ({
    firstDayOfWeek: authentication.user.firstDayOfWeekNo,
    timeZone: authentication.user.timeZone,
    locale: authentication.user.locale,
});

export default connect(mapStateToProps)(CalendarPanel);
