import React from 'react';
import { connect } from 'react-redux';
import { Button } from 'reactstrap';
import { DynamicLayoutContext } from '../../../context';

function AccessTableComponent() {
    const { data, callAction } = React.useContext(DynamicLayoutContext);

    let accessManagementEntry;
    let tasksEntry;
    let timesheetsEntry;
    let ownTimesheetsEntry;

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

    for (const [index, accessEntry] of data.accessEntries.entries()) {
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
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="accessInsert"
                                            defaultChecked={accessManagementEntry.accessInsert}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="accessUpdate"
                                            defaultChecked={accessManagementEntry.accessUpdate}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="accessDelete"
                                            defaultChecked={accessManagementEntry.accessDelete}
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
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="taskInsert"
                                            defaultChecked={tasksEntry.accessInsert}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="taskUpdate"
                                            defaultChecked={tasksEntry.accessUpdate}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="taskDelete"
                                            defaultChecked={tasksEntry.accessDelete}
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
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="timeSheetInsert"
                                            defaultChecked={timesheetsEntry.accessInsert}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="timeSheetUpdate"
                                            defaultChecked={timesheetsEntry.accessUpdate}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="timeSheetDelete"
                                            defaultChecked={timesheetsEntry.accessDelete}
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
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="ownTimeSheetInsert"
                                            defaultChecked={ownTimesheetsEntry.accessInsert}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="ownTimeSheetUpdate"
                                            defaultChecked={ownTimesheetsEntry.accessUpdate}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="ownTimeSheetDelete"
                                            defaultChecked={ownTimesheetsEntry.accessDelete}
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

const mapStateToProps = ({ authentication }) => ({
    user: authentication.user,
});


export default connect(mapStateToProps)(AccessTableComponent);
