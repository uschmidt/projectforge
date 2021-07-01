import PropTypes from 'prop-types';
import React from 'react';
import fileDownload from 'js-file-download';
import { UncontrolledTooltip } from 'reactstrap';
import { getServiceURL, handleHTTPErrors } from '../../../../../utilities/rest';
import DropArea from '../../../../design/droparea';
import LoadingContainer from '../../../../design/loading-container';

function DynamicAttachmentList(
    {
        id,
        title,
        uploadUrl,
        tooltip,
    },
) {
    const [loading, setLoading] = React.useState(false);

    const uploadFile = (files) => {
        setLoading(true);
        const formData = new FormData();
        let filename;
        let status = 0;
        formData.append('file', files[0]);
        return fetch(
            getServiceURL(`${uploadUrl}`),
            {
                credentials: 'include',
                method: 'POST',
                body: formData,
            },
        )
            .then(handleHTTPErrors)
            .then((response) => {
                ({ status } = response);
                if (response.headers.get('Content-Type')
                    .includes('application/json')) {
                    return response.json();
                }
                if (response.headers.get('Content-Type').includes('application/octet-stream')) {
                    filename = Object.getResponseHeaderFilename(response.headers.get('Content-Disposition'));
                    return response.blob();
                }
                throw Error(`Error ${status}`);
            })
            .then((result) => {
                setLoading(false);
                if (filename) {
                    // result as blob expected:
                    return fileDownload(result, filename);
                }
                throw Error('Not yet implemented.');
            })
            .catch((catchError) => {
                // eslint-disable-next-line no-alert
                alert(catchError);
                setLoading(false);
            });
    };

    return React.useMemo(() => (
        <LoadingContainer loading={loading}>
            <DropArea
                id={id}
                setFiles={uploadFile}
                noStyle
                title={title}
            />
            {tooltip && id && (
                <UncontrolledTooltip placement="auto" target={id}>
                    {tooltip}
                </UncontrolledTooltip>
            )}
        </LoadingContainer>
    ), [loading]);
}

DynamicAttachmentList.propTypes = {
    id: PropTypes.string,
    title: PropTypes.string.isRequired,
    uploadUrl: PropTypes.string.isRequired,
    tooltip: PropTypes.string,
};

DynamicAttachmentList.defaultProps = {
    id: undefined,
    tooltip: undefined,
};

export default DynamicAttachmentList;