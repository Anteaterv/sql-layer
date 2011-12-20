/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.server.expression.std;

import com.akiban.server.error.InvalidIntervalFormatException;
import com.akiban.server.expression.Expression;
import com.akiban.server.expression.ExpressionEvaluation;
import com.akiban.server.types.AkType;
import com.akiban.server.types.ValueSource;
import com.akiban.server.types.NullValueSource;

import static com.akiban.server.types.AkType.*;

public class IntervalCastExpression extends AbstractUnaryExpression
{
    protected static enum EndPoint
    {
        YEAR(INTERVAL_MONTH), 
        MONTH(INTERVAL_MONTH), 
        YEAR_MONTH(INTERVAL_MONTH), 
        DAY(INTERVAL_MILLIS), 
        HOUR(INTERVAL_MILLIS), 
        MINUTE(INTERVAL_MILLIS), 
        SECOND(INTERVAL_MILLIS), 
        DAY_HOUR(INTERVAL_MILLIS), 
        DAY_MINUTE(INTERVAL_MILLIS), 
        DAY_SECOND(INTERVAL_MILLIS),
        HOUR_MINUTE(INTERVAL_MILLIS), 
        HOUR_SECOND(INTERVAL_MILLIS),
        MINUTE_SECOND(INTERVAL_MILLIS);
        
        private EndPoint (AkType type)
        {
            this.type = type;
        }
        
        final AkType type;
    }

    private static class InnerEvaluation extends AbstractUnaryExpressionEvaluation
    {
        private final EndPoint endPoint;
        private static final long MULS[] = { 86400000L, 3600000L, 60000L, 1000L};
        
        public InnerEvaluation (ExpressionEvaluation ev, EndPoint endPoint)
        {
            super(ev);
            this.endPoint = endPoint;
        }

        @Override
        public ValueSource eval() 
        {
            ValueSource source = operand();
            if (source.isNull()) return NullValueSource.only();
            String interval = source.getString().trim();
            
            long result = 0;
            
            try
            {
                switch(endPoint)
                {
                    case YEAR: 
                        result = Long.parseLong(interval)  * 12; 
                        break;
                    case MONTH: 
                        result = Long.parseLong(interval); 
                        break;
                    case YEAR_MONTH: 
                        String yr_mth[] = interval.split("-");
                        if (yr_mth.length != 2) 
                            throw new InvalidIntervalFormatException (endPoint.name(), interval);
                        result = Long.parseLong(yr_mth[0]) * 12 + Long.parseLong(yr_mth[1]);
                        break;
                    case DAY:
                        result = Long.parseLong(interval) * MULS[0]; 
                        break;
                    case HOUR:
                        result = Long.parseLong(interval) * MULS[1]; 
                        break;
                    case MINUTE:
                        result = Long.parseLong(interval) * MULS[2]; 
                        break;
                    case SECOND:
                        
                        result = Math.round(Double.parseDouble(interval) * 1000);
                        break;
                    case DAY_HOUR:
                        result = getResult(0, interval, "\\s+", 2, false);
                        break;
                    case DAY_MINUTE:
                        result = getResult(0, interval, "\\s+|:", 3, false);
                        break;
                    case DAY_SECOND:
                        result = getResult(0, interval, "\\s+|:", 4, true);
                        break;
                    case HOUR_MINUTE:
                        result = getResult(1,interval, ":", 2, false);
                        break;
                    case HOUR_SECOND:
                        result = getResult(1, interval, ":", 3, true);
                        break;
                    default: // MINUTE_SECOND
                        result = getResult(2, interval, ":", 2, true);
                        break;
                }
                
                valueHolder().putRaw(endPoint.type, result);
                return valueHolder();
            } 
            catch (NumberFormatException ex)
            {
                throw new InvalidIntervalFormatException (endPoint.name(), interval);
            }
        }
        
        private long getResult (int start, String interval, String del, int expSize, boolean lastIsSec )
        {
            String strings[] = interval.split(del);
            if (strings.length != expSize) 
                throw new InvalidIntervalFormatException (endPoint.name(), interval);
            long res = 0;
            for (int n = 0, m = start; n < expSize - (lastIsSec ? 1 : 0); ++n, ++m)
                res += Long.parseLong(strings[n]) * MULS[m];
            if (lastIsSec)
                res += Math.round(Double.parseDouble(strings[expSize-1]) * 1000L);
            
            return res;
        }
        
    }
    
    private final EndPoint endPoint;
    protected IntervalCastExpression (Expression str, EndPoint endPoint)
    {
        super(endPoint.type, str);
        this.endPoint = endPoint;
    }
    
    @Override
    protected String name() 
    {
        return "CAST_INTERVAL_" + endPoint;
    }

    @Override
    public ExpressionEvaluation evaluation() 
    {
        return new InnerEvaluation(operandEvaluation(), endPoint);
    } 
}
