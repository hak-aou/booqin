@param String error
@param String id
@param String status
@param String chargeId
@param String balance_transaction

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>Result</title>
    </head>
    <body>
    @if(error != null)
        <h3 style="color: red;">${error}</h3>
    @else
        <h3 style="color: green;">Success!</h3>
        <div>Id.: <span>${id}</span></div>
        <div>Status: <span>${status}</span></div>
        <div>Charge id.: <span>${chargeId}</span></div>
        <div>Balance transaction id.: <span>${balance_transaction}</span></div>
    @endif

    </body>
</html>