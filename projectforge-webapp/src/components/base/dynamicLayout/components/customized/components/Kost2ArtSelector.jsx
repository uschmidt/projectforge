import React from 'react';
import PropTypes from 'prop-types';
import { DynamicLayoutContext } from '../../../context';

function Kost2ArtSelector() {
    const { data, setData, callAction } = React.useContext(DynamicLayoutContext);

    let entries = data.kost2Arts;

    const updateList = () => {
        // console.log(event.target.value)
        setData({ kost2Arts: entries });
    };

    function saveSelection(event) {
        // console.log(event.target.checked);
        entries = entries.map((item) => {
            if (item.name === event.target.id) {
                const updatedItem = {
                    ...item,
                    existsAlready: event.target.checked,
                };

                return updatedItem;
            }

            return item;
        });
        updateList();
    }

    return React.useMemo(
        () => (
            <React.Fragment>
                <div>
                    {entries.map(kost2Art => (
                        <div>
                            <input
                                type="checkbox"
                                id={kost2Art.name}
                                defaultChecked={kost2Art.existsAlready}
                                onChange={event => saveSelection(event)}
                            />
                            {kost2Art.name}
                        </div>
                    ))}
                </div>
            </React.Fragment>
        ),
        [data, callAction],
    );
}

Kost2ArtSelector.propTypes = {
    entries: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.number,
        name: PropTypes.string,
        description: PropTypes.string,
        workFraction: PropTypes.number,
        projektStandard: PropTypes.bool,
        fakturiert: PropTypes.bool,
        selected: PropTypes.bool,
        existsAlready: PropTypes.bool,
    })),
};

Kost2ArtSelector.defaultProps = {
    entries: undefined,
};

export default Kost2ArtSelector;
