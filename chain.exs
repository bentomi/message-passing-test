defmodule Chain do
  def relay(next_pid) do
    receive do
      message ->
        send next_pid, message
        relay(next_pid)
    end
  end

  def create_senders(m) do
    Enum.reduce 1..m, self(), fn (_, prev) -> spawn(Chain, :relay, [prev]) end
  end

  def test(m, n) do
    message = Enum.to_list 1..20
    start = create_senders(m)
    Enum.each 1..n, fn (_) -> send start, message end
    Enum.each 1..n, fn (_) -> receive do x -> x end end
  end

  def run(m, n) do
    IO.puts inspect :timer.tc(Chain, :test, [m, n])
  end
end
