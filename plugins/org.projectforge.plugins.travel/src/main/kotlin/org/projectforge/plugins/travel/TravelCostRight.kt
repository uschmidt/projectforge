package org.projectforge.plugins.travel

import org.projectforge.business.user.UserRightAccessCheck
import org.projectforge.business.user.UserRightCategory
import org.projectforge.business.user.UserRightValue
import org.projectforge.framework.access.AccessChecker
import org.projectforge.framework.access.OperationType
import org.projectforge.framework.persistence.user.entities.PFUserDO

class TravelCostRight: UserRightAccessCheck<TravelCostDO> {

    constructor(accessChecker: AccessChecker) : super(accessChecker, TravelPluginUserRightId.PLUGIN_TRAVEL, UserRightCategory.PLUGINS, UserRightValue.TRUE)

    /**
     * @return true if the owner is equals to the logged-in user, otherwise false.
     */
    override fun hasAccess(user: PFUserDO, obj: TravelCostDO, oldObj: TravelCostDO?,
                           operationType: OperationType): Boolean {
        val memo = (oldObj?: obj)
                ?: return true // General insert and select access given by default.
        return user.id == memo.user!!.id
    }
}