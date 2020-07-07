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
import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  /*
   * Function to return a collection of time ranges where people are available to meet
   * 
   * @param events    A collection of events which has attendees, our results should avoid these events
   * @param request   A specification on the minimum duration, mandatory and optional attendees
   * 
   * @return          A collection of time ranges where attendees are available
   * 
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getAttendees().size() <= 0 && request.getOptionalAttendees().size() <= 0) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }  
    
    HashSet<String> mandatoryAttendees = new HashSet<String>(request.getAttendees());
    HashSet<String> optionalAttendees = new HashSet<String>(request.getOptionalAttendees());
    HashSet<String> allAttendees = Sets.newHashSet(Iterables.concat(mandatoryAttendees, optionalAttendees));
      
    String endOfDayTitle = "END_OF_DAY";
    String startOfDayTitle = "START_OF_DAY";
    Event endOfDay = new Event(endOfDayTitle, TimeRange.fromStartDuration(TimeRange.END_OF_DAY + 1, 0), allAttendees);
    Event startOfDay = new Event(startOfDayTitle, TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0), allAttendees);

    ArrayList<Event> allEvents = new ArrayList<Event>(events);

    allEvents.add(endOfDay);
    allEvents.add(startOfDay);
      
    allEvents.sort(Event.EVENT_COMPARATOR);
    
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


  /*
   * Method to get available times from sorted events
   * Does this by removing events where there's no attendees, then merging events
   * which share some time. Finally the gaps are the result.
   * 
   * Runtime: O(n)
   *
  */
  public ArrayList<TimeRange> availableTimesFromSortedEvents(HashSet<String> attendees, ArrayList<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> availableTimes = new ArrayList<>();
    ArrayList<Event> sequentialEvents = new ArrayList<>(events);

    // Removes events in which no attendees intersect between the event and requested attendees
    for (int i = 0; i < sequentialEvents.size(); ++i) {
      HashSet<String> attendeesForEvent = new HashSet<String>(sequentialEvents.get(i).getAttendees());

      attendeesForEvent.retainAll(attendees);

      if (attendeesForEvent.size() <= 0) {
        sequentialEvents.remove(i);
        --i;
      }
    }
    

    // Merges events which share some time
    // Ex:       |---A---|
    //                |--B--|
    // Becomes:  |----------| <- MERGED_EVENT
    for (int i = 0; i < sequentialEvents.size() - 1;) {
      Event eventToCheck = sequentialEvents.get(i);
      Event nextEvent = sequentialEvents.get(i + 1);
      
      TimeRange eventTimeRange = eventToCheck.getWhen();
      TimeRange nextEventTimeRange = nextEvent.getWhen();

      if (eventTimeRange.overlaps(nextEventTimeRange)) {
        sequentialEvents.remove(i);
        sequentialEvents.remove(i);
        TimeRange mergedTimeRange = TimeRange.fromStartEnd(eventTimeRange.start(), Math.max(eventTimeRange.end(), nextEventTimeRange.end()), false);
        Event mergedEvent = new Event("MERGED_EVENT", mergedTimeRange, attendees);
        sequentialEvents.add(i, mergedEvent);
      } else {
        ++i;
      }
    }
     
    // Takes all gaps between events and becomes result
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
