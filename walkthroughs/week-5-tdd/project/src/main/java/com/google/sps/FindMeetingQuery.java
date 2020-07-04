// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import com.google.common.collect.Iterables; 
import com.google.common.collect.Sets;
import com.google.sps.Event;
import com.google.sps.TimeRange;
import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  public static final Comparator<Event> EVENT_COMPARATOR = new Comparator<Event>() {

    @Override
    public int compare(Event a, Event b) {
      if (a.getWhen().start() != b.getWhen().start())
        return TimeRange.ORDER_BY_START.compare(a.getWhen(), b.getWhen());
      else
        return TimeRange.ORDER_BY_END.compare(a.getWhen(), b.getWhen());
    }
  };
    
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getAttendees().size() <= 0 && request.getOptionalAttendees().size() <= 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }  
    
    HashSet<String> mandatoryAttendees = new HashSet<String>(request.getAttendees());
    HashSet<String> optionalAttendees = new HashSet<String>(request.getOptionalAttendees());
    HashSet<String> allAttendees = Sets.newHashSet(Iterables.concat(mandatoryAttendees, optionalAttendees));
      
    Event endOfDay = new Event("END_OF_DAY", TimeRange.fromStartDuration(TimeRange.END_OF_DAY + 1, 0), allAttendees);
    Event startOfDay = new Event("START_OF_DAY", TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0), allAttendees);

    ArrayList<Event> allEvents = new ArrayList<Event>(events);

    allEvents.add(endOfDay);
    allEvents.add(startOfDay);
      
    allEvents.sort(EVENT_COMPARATOR);
    


    ArrayList<TimeRange> available = availableTimesFromSortedEvents(mandatoryAttendees, allEvents, request);

    if (optionalAttendees.size() == 0) {
      return available;
    }

    ArrayList<TimeRange> availableWithOptional = availableTimesFromSortedEvents(optionalAttendees, allEvents, request);
    
    if (available.size() == 0) {
      return availableWithOptional;
    }
    
    ArrayList<TimeRange> finalTimeRanges = new ArrayList<>();

    for (TimeRange optionalTimeRange: availableWithOptional) {
      int start = optionalTimeRange.start();
      int end = optionalTimeRange.end();

      int indexOfIntersection = binarySearchOverlappingTimeRange(available, 0, available.size() - 1, start);

      if (indexOfIntersection != -1) {
        int mandatoryEnd = available.get(indexOfIntersection).end();
        if (end <= mandatoryEnd) {
          finalTimeRanges.add(optionalTimeRange);
        }
      }
    }

    return finalTimeRanges.size() != 0 ? finalTimeRanges : available;
  }
    
  public int binarySearchOverlappingTimeRange(ArrayList<TimeRange> mandatory, int startIndex, int endIndex, int startOfOptional) {
    int startOfMandatory = mandatory.get(startIndex).start();

    if (startIndex == endIndex) {
      return startOfMandatory <= startOfOptional ? startIndex : -1;  
    }

    int midIndex = startIndex + (endIndex - startIndex) / 2;

    if (startOfOptional < mandatory.get(midIndex).start()) {
      return binarySearchOverlappingTimeRange(mandatory, startIndex, midIndex, startOfOptional);
    }

    int ret = binarySearchOverlappingTimeRange(mandatory, midIndex + 1, endIndex, startOfOptional);

    return ret == -1 ? midIndex : ret;
  }

  public ArrayList<TimeRange> availableTimesFromSortedEvents(HashSet<String> attendees, ArrayList<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> availableTimes = new ArrayList<>();
    ArrayList<Event> sequentialEvents = new ArrayList<>(events);

    for (int i = 0; i < sequentialEvents.size(); ++i) {
      HashSet<String> attendeesForEvent = new HashSet<String>(sequentialEvents.get(i).getAttendees());

      attendeesForEvent.retainAll(attendees);

      if (attendeesForEvent.size() <= 0) {
        sequentialEvents.remove(i);
        --i;
      }
    }
    
    for (int i = 0; i < sequentialEvents.size() - 1;) {
      Event eventToCheck = sequentialEvents.get(i);
      Event nextEvent = sequentialEvents.get(i + 1);
      
      int startTimeEvent = eventToCheck.getWhen().start();
      int endTimeEvent = eventToCheck.getWhen().end();
      int startTimeNextEvent = nextEvent.getWhen().start();
      int endTimeNextEvent = nextEvent.getWhen().end();
          
      if (endTimeEvent >= startTimeNextEvent) {
        sequentialEvents.remove(i);
        sequentialEvents.remove(i);
        TimeRange mergedTimeRange = TimeRange.fromStartDuration(startTimeEvent, Math.max(endTimeEvent, endTimeNextEvent) - startTimeEvent);
        Event mergedEvent = new Event("MERGED_EVENT", mergedTimeRange, attendees);
        sequentialEvents.add(i, mergedEvent);
      } else {
        ++i;
      }
    }
      
    for (int i = 0; i < sequentialEvents.size() - 1; ++i) {
      
      Event eventToCheck = sequentialEvents.get(i);
      Event nextEvent = sequentialEvents.get(i + 1);
      
      int endTimeEvent = eventToCheck.getWhen().end();
      int startTimeNextEvent = nextEvent.getWhen().start();
      
      int duration = startTimeNextEvent - endTimeEvent;
      

      if (duration >= request.getDuration()) {
        availableTimes.add(TimeRange.fromStartDuration(endTimeEvent, duration));
      }
    }
      
    return availableTimes;
  }
}