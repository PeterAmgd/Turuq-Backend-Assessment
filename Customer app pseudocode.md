function bookDeliverySlot(customerId, preferredSlotId):
    
    steps we will apply them with sequence
                    the flow in steps
    Step 1 : request validation *maybe here and maybe within the request class*
    Step 2 : retrieve Slot *get slot data*
    Step 3 : lock slot *prevent multiple customers from booking the same slot simultaneously to prevent race condition*
    Step 4 : check availability *check if slot capacity not fully completed*
             *if completed -> return the available slots*   
    Step 5 : reserve slot
    Step 6 : create booking
    Step 7 : release lock
    Step 8 : return success response
    
    if customerId is null:
        return Error("Customer ID is required")

    if preferredSlotId is null:
        return Error("Delivery slot is required")


    
    Step 2 : Retrieve Slot
    

    slot = findSlotById(preferredSlotId)

    if slot does not exist:
        return Error("Selected delivery slot does not exist")


    
    Step 3
    acquireLock(slot.id)


    
    Step 4 
    if slot.bookedCount >= slot.capacity:

        releaseLock(slot.id)

        alternativeSlots = findAlternativeSlots(slot.date)

        return Response(
            success = false,
            message = "Selected slot is fully booked.",
            alternatives = alternativeSlots
        )


    
    Step 5

    slot.bookedCount = slot.bookedCount + 1
    save(slot)


    
    Step 6
    booking = createBooking(
        customerId,
        slot.id,
        currentTime
    )
    
    Step 7
    releaseLock(slot.id)

    Step 8
    return Response(
        success = true,
        message = "Delivery slot booked successfully.",
        bookingId = booking.id,
        slot = slot
    )




Find Alternative Slots


function findAlternativeSlots(date):

    availableSlots = []

    slots = getSlotsForDate(date)

    for each slot in slots:

        if slot.bookedCount < slot.capacity:

            availableSlots.add(slot)

    return availableSlots




Display Slots


function getDeliverySlots(date):

    slots = getSlotsForDate(date)

    result = []

    for each slot in slots:

        if slot.bookedCount >= slot.capacity:

            status = "FULL"

        else

            status = "AVAILABLE"

        result.add(
            slot.id,
            slot.startTime,
            slot.endTime,
            status,
            remainingCapacity =
                slot.capacity - slot.bookedCount
        )

    return result