package com.mobilize.fastdao;

/**
 * Created by o-emorales on 13/09/2017.
 */

/**
 * Created by o-emorales on 06/09/2017.
 */

/**
 * The Interface DbModel, to be implemented for mapping table with db table .
 */
public interface DbModel {

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId();

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(long id);

    /**
     * returns table name to which the ordersModel corresponds.
     *
     * @return the table name
     */
    public String getTableName();

    /**
     * Gets the creates the statement for table.
     *
     * @return the creates the statement
     */
    public String getCreateStatement();
}
