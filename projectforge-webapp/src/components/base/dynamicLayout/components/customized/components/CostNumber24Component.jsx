import React from 'react';
import { connect } from 'react-redux';
import { DynamicLayoutContext } from '../../../context';

function CostNumber24Component({ values }) {
    const { data, setData } = React.useContext(DynamicLayoutContext);
    const {
        bereich, nummer,
    } = values;

    const handleBereichChange = (event) => {
        // console.log(event.target.value)
        setData({ bereich: event.target.value });
    };

    const handleNummerChange = (event) => {
        // console.log(event.target.value)
        setData({ nummer: event.target.value });
    };


    return React.useMemo(
        () => (
            <React.Fragment>
                Projektnummer
                <br />
                {data.nummernkreis}
                .
                <input
                    id="bereich"
                    type="number"
                    size="3"
                    min="0"
                    max="999"
                    defaultValue={bereich}
                    onChange={handleBereichChange}
                />
                .
                <input
                    id="nummer"
                    type="number"
                    size="2"
                    min="0"
                    max="99"
                    defaultValue={nummer}
                    onChange={handleNummerChange}
                />
                .
                ##
            </React.Fragment>
        ), [data],
    );
}

const mapStateToProps = ({ authentication }) => ({
    user: authentication.user,
    jsTimestampFormatMinutes: authentication.user.jsTimestampFormatMinutes,
});


export default connect(mapStateToProps)(CostNumber24Component);
