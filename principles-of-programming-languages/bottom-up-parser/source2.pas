program Source2

  var
    s: Integer;
    i: Integer;
    acc: Integer

  begin

    read s;
    i := 1;
    acc := 0;
    while i <= s do
    begin
        acc := acc + i;
        i := i + 1
    end

  end.
