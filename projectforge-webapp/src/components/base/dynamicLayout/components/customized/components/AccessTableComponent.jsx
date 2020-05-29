import React from 'react';
import { connect } from 'react-redux';
import { Button } from 'reactstrap';
import { DynamicLayoutContext } from '../../../context';

function AccessTableComponent() {
    const { data, callAction } = React.useContext(DynamicLayoutContext);

    let accessManagement = [false, true, false, false];
    let tasks = [false, false, false, false];
    let timesheets = [false, false, false, false];
    let ownTimesheets = [false, false, false, false];

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
            tasks = [accessEntry.accessSelect, accessEntry.accessInsert,
                accessEntry.accessUpdate, accessEntry.accessDelete];
        }

        if (accessEntry.accessType === 'TIMESHEETS') {
            timesheets = [accessEntry.accessSelect, accessEntry.accessInsert,
                accessEntry.accessUpdate, accessEntry.accessDelete];
        }

        if (accessEntry.accessType === 'OWN_TIMESHEETS') {
            ownTimesheets = [accessEntry.accessSelect, accessEntry.accessInsert,
                accessEntry.accessUpdate, accessEntry.accessDelete];
        }

        if (accessEntry.accessType === 'TASK_ACCESS_MANAGEMENT') {
            accessManagement = [accessEntry.accessSelect, accessEntry.accessInsert,
                accessEntry.accessUpdate, accessEntry.accessDelete];
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
                                            id="id10"
                                            defaultChecked={accessManagement[0]}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id11"
                                            defaultChecked={accessManagement[1]}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id12"
                                            defaultChecked={accessManagement[2]}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id13"
                                            defaultChecked={accessManagement[3]}
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
                                            id="id14"
                                            defaultChecked={tasks[0]}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id15"
                                            defaultChecked={tasks[1]}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id16"
                                            defaultChecked={tasks[2]}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id17"
                                            defaultChecked={tasks[3]}
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
                                            id="id18"
                                            defaultChecked={timesheets[0]}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id19"
                                            defaultChecked={timesheets[1]}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1a"
                                            defaultChecked={timesheets[2]}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1b"
                                            defaultChecked={timesheets[3]}
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
                                            id="id1c"
                                            defaultChecked={ownTimesheets[0]}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1d"
                                            defaultChecked={ownTimesheets[1]}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1e"
                                            defaultChecked={ownTimesheets[2]}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1f"
                                            defaultChecked={ownTimesheets[3]}
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
            callAction,
            ownTimesheets,
            timesheets,
            tasks,
            accessManagement],
    );
}

const mapStateToProps = ({ authentication }) => ({
    user: authentication.user,
});


export default connect(mapStateToProps)(AccessTableComponent);
