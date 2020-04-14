package org.projectforge.plugins.travel

import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrBaseDO
import de.micromata.genome.db.jpa.tabattr.entities.JpaTabAttrDataBaseDO
import javax.persistence.*

@Entity
open class TravelKostAttrDO : JpaTabAttrBaseDO<TravelKostDO, Int> {
    constructor() : super() {}

    constructor(parent: TravelKostDO, propertyName: String, type: Char, value: String) : super(parent, propertyName, type, value) {}

    override fun createData(data: String): JpaTabAttrDataBaseDO<*, Int> {
        return TravelKostAttrDataDO(this, data)
    }

    @Id
    @GeneratedValue
    @Column(name = "pk")
    override fun getPk(): Int? {
        return pk
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "parent", referencedColumnName = "pk")
    override fun getParent(): TravelKostDO? {
        return super.getParent()
    }
}
