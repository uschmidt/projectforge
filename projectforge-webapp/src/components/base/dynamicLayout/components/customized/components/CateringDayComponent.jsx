import React from 'react';
import { Table } from 'reactstrap';
import { DynamicLayoutContext } from '../../../context';
import CateringDayEntries from './CateringDayEntries';

function CateringDayComponent() {
    const { data, ui } = React.useContext(DynamicLayoutContext);

    return (
        <React.Fragment>
            <h4>{ui.translations['plugins.travel.entry.catering.list']}</h4>
            <Table striped hover>
                <thead>
                    <tr>
                        <th>{ui.translations['plugins.travel.entry.travelday']}</th>
                        <th>{ui.translations['plugins.travel.entry.catering.list.breakfast']}</th>
                        <th>{ui.translations['plugins.travel.entry.catering.list.lunch']}</th>
                        <th>{ui.translations['plugins.travel.entry.catering.list.dinner']}</th>
                    </tr>
                </thead>
                <tbody>
                    <CateringDayEntries entries={data.catering} />
                </tbody>
            </Table>
        </React.Fragment>
    );
}

CateringDayComponent.propTypes = {};

CateringDayComponent.defaultProps = {};
