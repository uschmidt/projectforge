package org.projectforge.plugins.travel

import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrDataBaseDO
import javax.persistence.*

@Entity
class TravelKostAttrDataDO : JpaTabAttrDataBaseDO<TravelKostAttrDO, Int> {
    constructor() : super()

    constructor(parent: TravelKostAttrDO, value: String) : super(parent, value)

    @Id
    @GeneratedValue
    @Column(name = "pk")
    override fun getPk(): Int? {
        return pk
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_id", referencedColumnName = "pk")
    override fun getParent(): TravelKostAttrDO {
        return super.getParent()
    }
}