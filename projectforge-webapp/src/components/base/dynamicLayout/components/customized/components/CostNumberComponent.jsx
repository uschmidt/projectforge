import React from 'react';
import { connect } from 'react-redux';
import { DynamicLayoutContext } from '../../../context';

function CostNumberComponent({ values }) {
    const { data, setData } = React.useContext(DynamicLayoutContext);
    const {
        nummernkreis, bereich, teilbereich, endziffer,
    } = values;

    const handleNummernkreisChange = (event) => {
        // console.log(event.target.value)
        setData({ nummernkreis: event.target.value });
    };

    const handleBereichChange = (event) => {
        // console.log(event.target.value)
        setData({ bereich: event.target.value });
    };

    const handleTeilbereichChange = (event) => {
        // console.log(event.target.value)
        setData({ teilbereich: event.target.value });
    };

    const handleEndzifferChange = (event) => {
        // console.log(event.target.value)
        setData({ endziffer: event.target.value });
    };


    return React.useMemo(
        () => (
            <React.Fragment>
                <input
                    id="nummernkreis"
                    type="number"
                    size="1"
                    maxLength="1"
                    min="0"
                    max="9"
                    defaultValue={nummernkreis.toString()}
                    onChange={handleNummernkreisChange}
                />
                .
                <input
                    id="bereich"
                    type="number"
                    size="3"
                    min="0"
                    max="999"
                    defaultValue={bereich.toString()}
                    onChange={handleBereichChange}
                />
                .
                <input
                    id="teilbereich"
                    type="number"
                    size="2"
                    min="0"
                    max="99"
                    defaultValue={teilbereich.toString()}
                    onChange={handleTeilbereichChange}
                />
                .
                <input
                    id="endziffer"
                    type="number"
                    size="2"
                    min="0"
                    max="99"
                    defaultValue={endziffer.toString()}
                    onChange={handleEndzifferChange}
                />
            </React.Fragment>
        ), [data],
    );
}

const mapStateToProps = ({ authentication }) => ({
    user: authentication.user,
    jsTimestampFormatMinutes: authentication.user.jsTimestampFormatMinutes,
});


export default connect(mapStateToProps)(CostNumberComponent);
