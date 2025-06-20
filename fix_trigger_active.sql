-- Corrected Payment Completion Trigger - Sets status to 'Aktif' not 'Odendi'
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
    
    -- If we have a completed payment, update rental status to 'Aktif' (ID: 2)
    IF @RentalID IS NOT NULL
    BEGIN
        UPDATE Rentals 
        SET RentalStatusID = 2,  -- Aktif status
            UpdatedDate = GETDATE()
        WHERE RentalID = @RentalID;
    END
END; 