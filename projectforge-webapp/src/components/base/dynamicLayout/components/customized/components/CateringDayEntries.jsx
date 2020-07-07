import PropTypes from 'prop-types';
import React from 'react';

function VacationEntries({ entries }) {
    return (
        <React.Fragment>
            {entries && entries > 0 && entries.map((entry, index) => (
                <tr>
                    <td>{entry.dateFormatted}</td>
                    <td>{entry.breakfast}</td>
                    <td>{entry.lunch}</td>
                    <td>{entry.dinner}</td>
                </tr>
            ))}
        </React.Fragment>
    );
}

VacationEntries.propTypes = {
    entries: PropTypes.arrayOf(PropTypes.shape({
        dateFormatted: PropTypes.string,
        breakfast: PropTypes.bool,
        lunch: PropTypes.bool,
        dinner: PropTypes.bool,
    })),
};

VacationEntries.defaultProps = {
    entries: undefined,
};

export default VacationEntries;
