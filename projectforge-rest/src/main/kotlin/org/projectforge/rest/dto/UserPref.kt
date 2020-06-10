package org.projectforge.rest.dto

import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.projectforge.framework.persistence.user.entities.UserPrefDO

class UserPref(
        var user: PFUserDO? = null,
        var name: String? = null,
        var area: String? = null
): BaseDTO<UserPrefDO>() {

}
