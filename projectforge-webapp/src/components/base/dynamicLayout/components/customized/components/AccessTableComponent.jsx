import React from 'react';
import { Button } from 'reactstrap';
import PropTypes from 'prop-types';
import { DynamicLayoutContext } from '../../../context';

function AccessTableComponent() {
    const { data, setData, callAction } = React.useContext(DynamicLayoutContext);

    let entries = data.accessEntries;

    let accessManagementEntry;
    let tasksEntry;
    let timesheetsEntry;
    let ownTimesheetsEntry;

    const updateList = () => {
        // console.log(event.target.value)
        setData({ accessEntries: entries });
    };

    function setSelectAccess(event, accessType) {
        // console.log(event.target.checked);
        entries = entries.map((item) => {
            if (item.accessType === accessType) {
                const updatedItem = {
                    ...item,
                    accessSelect: event.target.checked,
                };

                return updatedItem;
            }

            return item;
        });
        updateList();
    }

    function setInsertAccess(event, accessType) {
        // console.log(event.target.checked);
        entries = entries.map((item) => {
            if (item.accessType === accessType) {
                const updatedItem = {
                    ...item,
                    accessInsert: event.target.checked,
                };

                return updatedItem;
            }

            return item;
        });
        updateList();
    }

    function setUpdateAccess(event, accessType) {
        // console.log(event.target.checked);
        entries = entries.map((item) => {
            if (item.accessType === accessType) {
                const updatedItem = {
                    ...item,
                    accessUpdate: event.target.checked,
                };

                return updatedItem;
            }

            return item;
        });
        updateList();
    }

    function setDeleteAccess(event, accessType) {
        // console.log(event.target.checked);
        entries = entries.map((item) => {
            if (item.accessType === accessType) {
                const updatedItem = {
                    ...item,
                    accessDelete: event.target.checked,
                };

                return updatedItem;
            }

            return item;
        });
        updateList();
    }

    const clear = () => callAction({
        responseAction: {
            url: 'access/template/clear',
            targetType: 'POST',
        },
    });

    const guest = () => callAction({
        responseAction: {
            url: 'access/template/guest',
            targetType: 'POST',
        },
    });

    const employee = () => callAction({
        responseAction: {
            url: 'access/template/employee',
            targetType: 'POST',
        },
    });

    const leader = () => callAction({
        responseAction: {
            url: 'access/template/leader',
            targetType: 'POST',
        },
    });

    const administrator = () => callAction({
        responseAction: {
            url: 'access/template/administrator',
            targetType: 'POST',
        },
    });

    for (const [index, accessEntry] of entries.entries()) {
        if (accessEntry.accessType === 'TASKS') {
            tasksEntry = accessEntry;
        }

        if (accessEntry.accessType === 'TIMESHEETS') {
            timesheetsEntry = accessEntry;
        }

        if (accessEntry.accessType === 'OWN_TIMESHEETS') {
            ownTimesheetsEntry = accessEntry;
        }

        if (accessEntry.accessType === 'TASK_ACCESS_MANAGEMENT') {
            accessManagementEntry = accessEntry;
        }
    }

    return React.useMemo(
        () => (
            <React.Fragment>
                <table>
                    <tbody>
                        <tr>
                            <th>Access management</th>
                            <td>
                                <div className="btn-group" data-toggle="buttons">

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="accessSelect"
                                            defaultChecked={accessManagementEntry.accessSelect}
                                            onChange={event => setSelectAccess(event, 'TASK_ACCESS_MANAGEMENT')}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="accessInsert"
                                            defaultChecked={accessManagementEntry.accessInsert}
                                            onChange={event => setInsertAccess(event, 'TASK_ACCESS_MANAGEMENT')}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="accessUpdate"
                                            defaultChecked={accessManagementEntry.accessUpdate}
                                            onChange={event => setUpdateAccess(event, 'TASK_ACCESS_MANAGEMENT')}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="accessDelete"
                                            defaultChecked={accessManagementEntry.accessDelete}
                                            onChange={event => setDeleteAccess(event, 'TASK_ACCESS_MANAGEMENT')}
                                        />
                                        Delete
                                    </label>

                                </div>
                            </td>
                        </tr>

                        <tr>
                            <th>Structure elements</th>
                            <td>
                                <div className="btn-group" data-toggle="buttons">

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="taskSelect"
                                            defaultChecked={tasksEntry.accessSelect}
                                            onChange={event => setSelectAccess(event, 'TASKS')}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="taskInsert"
                                            defaultChecked={tasksEntry.accessInsert}
                                            onChange={event => setInsertAccess(event, 'TASKS')}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="taskUpdate"
                                            defaultChecked={tasksEntry.accessUpdate}
                                            onChange={event => setUpdateAccess(event, 'TASKS')}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="taskDelete"
                                            defaultChecked={tasksEntry.accessDelete}
                                            onChange={event => setDeleteAccess(event, 'TASKS')}
                                        />
                                        Delete
                                    </label>

                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th>Time sheets</th>
                            <td>
                                <div className="btn-group" data-toggle="buttons">

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="timeSheetSelect"
                                            defaultChecked={timesheetsEntry.accessSelect}
                                            onChange={event => setSelectAccess(event, 'TIMESHEETS')}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="timeSheetInsert"
                                            defaultChecked={timesheetsEntry.accessInsert}
                                            onChange={event => setInsertAccess(event, 'TIMESHEETS')}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="timeSheetUpdate"
                                            defaultChecked={timesheetsEntry.accessUpdate}
                                            onChange={event => setUpdateAccess(event, 'TIMESHEETS')}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="timeSheetDelete"
                                            defaultChecked={timesheetsEntry.accessDelete}
                                            onChange={event => setDeleteAccess(event, 'TIMESHEETS')}
                                        />
                                        Delete
                                    </label>

                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th>Own time sheets</th>
                            <td>
                                <div className="btn-group" data-toggle="buttons">

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="ownTimeSheetSelect"
                                            defaultChecked={ownTimesheetsEntry.accessSelect}
                                            onChange={event => setSelectAccess(event, 'OWN_TIMESHEETS')}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="ownTimeSheetInsert"
                                            defaultChecked={ownTimesheetsEntry.accessInsert}
                                            onChange={event => setInsertAccess(event, 'OWN_TIMESHEETS')}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="ownTimeSheetUpdate"
                                            defaultChecked={ownTimesheetsEntry.accessUpdate}
                                            onChange={event => setUpdateAccess(event, 'OWN_TIMESHEETS')}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="ownTimeSheetDelete"
                                            defaultChecked={ownTimesheetsEntry.accessDelete}
                                            onChange={event => setDeleteAccess(event, 'OWN_TIMESHEETS')}
                                        />
                                        Delete
                                    </label>

                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <br />


                <div>
                    Access Templates

                    <Button id="quickselect_clear" onClick={clear}>
                        Clear
                    </Button>

                    <Button id="quickselect_guest" onClick={guest}>
                        Guest
                    </Button>

                    <Button id="quickselect_employee" onClick={employee}>
                        Employee
                    </Button>

                    <Button id="quickselect_leader" onClick={leader}>
                        Leader
                    </Button>

                    <Button id="quickselect_administrator" onClick={administrator}>
                        Administrator
                    </Button>
                </div>
            </React.Fragment>
        ), [data,
            callAction],
    );
}

AccessTableComponent.propTypes = {
    entries: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.number,
        accessType: PropTypes.string,
        accessSelect: PropTypes.bool,
        accessInsert: PropTypes.bool,
        accessUpdate: PropTypes.bool,
        accessDelete: PropTypes.bool,
    })),
};

AccessTableComponent.defaultProps = {
    entries: undefined,
};


export default AccessTableComponent;
