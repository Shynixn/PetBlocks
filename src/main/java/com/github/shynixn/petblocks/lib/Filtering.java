package com.github.shynixn.petblocks.lib;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;

@Deprecated
public class Filtering {
    public static void filterMessages() {
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new Filter() {
            @Override
            public Result filter(LogEvent event) {
                if (event.getMessage().toString().contains("Wrong location for CustomRabbit")
                        || event.getMessage().toString().contains("Wrong location for CustomGroundArmorstand")
                        || event.getMessage().toString().contains("but was stored in chunk")
                        || event.getMessage().toString().contains("Attempted Double World add on CustomGroundArmorstand")
                        || event.getMessage().toString().contains("Attempted Double World add on CustomRabbit")) {
                    return Result.DENY;
                }
                return null;
            }

            @Override
            public Result filter(org.apache.logging.log4j.core.Logger arg0,
                                 Level arg1, Marker arg2, String arg3, Object... arg4) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Result filter(org.apache.logging.log4j.core.Logger arg0,
                                 Level arg1, Marker arg2, Object arg3, Throwable arg4) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Result filter(org.apache.logging.log4j.core.Logger arg0,
                                 Level arg1, Marker arg2, Message arg3, Throwable arg4) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Result getOnMatch() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Result getOnMismatch() {
                // TODO Auto-generated method stub
                return null;
            }
        });
    }
}
