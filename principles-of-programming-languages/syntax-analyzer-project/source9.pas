program Source9

  var
    s: Integer;
    i: Integer;
    acc: Integer

  begin

    read s;
    i := 1;
    acc := 0;
    while i <= s
    begin
        acc := acc + i;
        i := i + 1
    end

  end.
