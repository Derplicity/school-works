program Source11

  var
    s: Integer;
    i: Integer;
    acc: Integer

  begin

    read s;
    i := 1;
    acc := 0;
    while i <= s do
        acc := acc + i;
        i := i + 1
    end

  end.
