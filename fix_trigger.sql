-- Simplified Payment Completion Trigger
CREATE TRIGGER tr_Payments_Completion
ON Payments
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @RentalID INT;
    
    -- Get the RentalID from the inserted payment with 'Completed' status
    SELECT @RentalID = RentalID 
    FROM inserted 
    WHERE PaymentStatus = 'Completed';
    
    -- If we have a completed payment, update rental status to 'Odendi' (ID: 6)
    IF @RentalID IS NOT NULL
    BEGIN
        UPDATE Rentals 
        SET RentalStatusID = 6,
            UpdatedDate = GETDATE()
        WHERE RentalID = @RentalID;
    END
END; 