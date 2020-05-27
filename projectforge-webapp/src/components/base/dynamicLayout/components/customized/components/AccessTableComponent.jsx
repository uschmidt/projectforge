import React from 'react';
import { connect } from 'react-redux';
import { Button } from 'reactstrap';
import { DynamicLayoutContext } from '../../../context';

function AccessTableComponent() {
    const { data, callAction } = React.useContext(DynamicLayoutContext);

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
                                            defaultChecked={data.accessEntries[3].accessSelect}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id11"
                                            defaultChecked={data.accessEntries[3].accessInsert}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id12"
                                            defaultChecked={data.accessEntries[3].accessUpdate}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id13"
                                            defaultChecked={data.accessEntries[3].accessDelete}
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
                                            defaultChecked={data.accessEntries[1].accessSelect}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id15"
                                            defaultChecked={data.accessEntries[1].accessInsert}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id16"
                                            defaultChecked={data.accessEntries[1].accessUpdate}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id17"
                                            defaultChecked={data.accessEntries[1].accessDelete}
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
                                            defaultChecked={data.accessEntries[0].accessSelect}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id19"
                                            defaultChecked={data.accessEntries[0].accessInsert}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1a"
                                            defaultChecked={data.accessEntries[0].accessUpdate}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1b"
                                            defaultChecked={data.accessEntries[0].accessDelete}
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
                                            defaultChecked={data.accessEntries[2].accessSelect}
                                        />
                                        Select
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1d"
                                            defaultChecked={data.accessEntries[2].accessInsert}
                                        />
                                        Insert
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1e"
                                            defaultChecked={data.accessEntries[2].accessUpdate}
                                        />
                                        Update
                                    </label>

                                    <label className="btn btn-xs btn-primary">
                                        <input
                                            type="checkbox"
                                            id="id1f"
                                            defaultChecked={data.accessEntries[2].accessDelete}
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
        ),
    );
}

const mapStateToProps = ({ authentication }) => ({
    user: authentication.user,
});


export default connect(mapStateToProps)(AccessTableComponent);
