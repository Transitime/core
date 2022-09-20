package org.transitclock.core.reporting;

public enum ReportingDataFileType {
    ARRIVALS_DEPARTURES{
        @Override
        public String fileName(){
          return "arrivals_departures";
        }

        @Override
        public boolean includeRoute(){
            return true;
        }
    },
    CALENDAR_DATES {
        @Override
        public String fileName(){
            return "calendar_dates";
        }

        @Override
        public boolean includeRoute(){
            return false;
        }
    },
    CALENDARS {
        @Override
        public String fileName(){
            return "calendars";
        }

        @Override
        public boolean includeRoute(){
            return false;
        }
    },
    RUNTIMES_FOR_ROUTES {
        @Override
        public String fileName(){
            return "runTimesForRoutes";
        }

        @Override
        public boolean includeRoute(){
            return true;
        }
    },
    RUNTIMES_FOR_STOPS {
        @Override
        public String fileName(){
            return "runTimesForStops";
        }

        @Override
        public boolean includeRoute(){
            return true;
        }
    },
    STOP_PATHS {
        @Override
        public String fileName(){
            return "stopPaths";
        }
        @Override
        public boolean includeRoute(){
            return true;
        }
    },
    TRIP_PATTERNS {
        @Override
        public String fileName(){
            return "trip_patterns";
        }

        @Override
        public boolean includeRoute(){
            return true;
        }
    },
    TRIP_SCHEDULE_TIMES {
        @Override
        public String fileName(){
            return "trip_schedule_times";
        }

        @Override
        public boolean includeRoute(){
            return true;
        }
    },
    TRIPS {
        @Override
        public String fileName(){
            return "trips";
        }

        @Override
        public boolean includeRoute(){
            return true;
        }
    },
    FEED_INFO {
        @Override
        public String fileName(){
            return "feed_info";
        }

        @Override
        public boolean includeRoute(){
            return false;
        }
    };

    public abstract String fileName();

    public abstract boolean includeRoute();

}
