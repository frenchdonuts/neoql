/*****************************************************************************************************
 **       ____  _____  ___  ____    __      ____  _____  _    _  ____  ____                         **
 **      (  _ \(  _  )/ __)( ___)  /__\    (  _ \(  _  )( \/\/ )( ___)(  _ \                        **
 **       )(_) ))(_)(( (__  )__)  /(__)\    )___/ )(_)(  )    (  )__)  )   /                        **
 **      (____/(_____)\___)(____)(__)(__)  (__)  (_____)(__/\__)(____)(_)\_)                        **
 **                                                                                                 **
 ** Created Jun 24, 2013 by : benoit.cantin@doceapower.com                                           **
 ** Copyright Docea Power 2013                                                                      **
 ** Any reproduction or distribution prohibited without express written permission from Docea Power **
 *****************************************************************************************************/
package net.ericaro.neoql.patches;

import net.ericaro.neoql.DDL;
import net.ericaro.neoql.DML;

/**
 * Often, only {@link DDL} operations are visited. Sometimes, only {@link DML} are interesting. Use this adapter to
 * benefit from a default implementation doing nothing.
 * 
 * @author Benoit Cantin
 */
public class PatchVisitorAdapter<U> implements PatchVisitor<U> {

    @Override
    public U visit(PatchSet patch) {
        return null;
    }

    @Override
    public <T> U visit(Delete<T> patch) {
        return null;
    }

    @Override
    public <T> U visit(Insert<T> patch) {
        return null;
    }

    @Override
    public <T> U visit(Update<T> patch) {
        return null;
    }

    @Override
    public U visit(CreateTable patch) {
        return null;
    }

    @Override
    public U visit(DropTable patch) {
        return null;
    }

}
