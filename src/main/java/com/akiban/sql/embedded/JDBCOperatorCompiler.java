/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.akiban.sql.embedded;

import com.akiban.sql.embedded.JDBCResultSetMetaData.JDBCResultColumn;

import com.akiban.sql.server.ServerOperatorCompiler;
import com.akiban.sql.server.ServerPlanContext;

import com.akiban.sql.optimizer.plan.BasePlannable;
import com.akiban.sql.optimizer.plan.PhysicalSelect;
import com.akiban.sql.optimizer.plan.PhysicalSelect.PhysicalResultColumn;
import com.akiban.sql.optimizer.plan.PhysicalUpdate;
import com.akiban.sql.optimizer.plan.ResultSet.ResultField;
import com.akiban.sql.optimizer.rule.PlanContext;

import com.akiban.sql.parser.*;
import com.akiban.sql.types.DataTypeDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.*;

public class JDBCOperatorCompiler extends ServerOperatorCompiler
{
    private static final Logger logger = LoggerFactory.getLogger(JDBCOperatorCompiler.class);

    protected JDBCOperatorCompiler() {
    }

    protected static JDBCOperatorCompiler create(JDBCConnection connection) {
        JDBCOperatorCompiler compiler = new JDBCOperatorCompiler();
        compiler.initServer(connection);
        compiler.initDone();
        return compiler;
    }

    @Override
    public PhysicalResultColumn getResultColumn(ResultField field) {
        int jdbcType = Types.OTHER;
        DataTypeDescriptor sqlType = field.getSQLtype();
        if (sqlType != null)
            jdbcType = sqlType.getJDBCTypeId();
        return new JDBCResultColumn(field.getName(), 
                                    jdbcType, sqlType,
                                    field.getAIScolumn(), field.getTInstance());
    }

}
