class PocketConfig:
    enter_threshold = 75
    exit_threshold = 40
    enter_debounce = 500
    exit_debounce = 200
    pitch_black_lux = 2.0
    dark_lux = 15.0

class State:
    OUT = 0; DETECTING = 1; IN = 2; REMOVING = 3

class Simulator:
    def __init__(self):
        self.state = State.OUT
        self.timer = 0
        self.time_ms = 0
        self.gravity = [0.0, 0.0, 9.8]
        self.in_pocket = False
        
    def tick(self, prox_near, lux, accel, dt_ms, has_light=True, has_gravity=True):
        self.time_ms += dt_ms
        if has_gravity:
            self.gravity[0] = 0.8 * self.gravity[0] + 0.2 * accel[0]
            self.gravity[1] = 0.8 * self.gravity[1] + 0.2 * accel[1]
            self.gravity[2] = 0.8 * self.gravity[2] + 0.2 * accel[2]

        conf = 0
        if prox_near:
            conf += 50
            if has_light:
                if lux < PocketConfig.pitch_black_lux: conf += 30
                elif lux < PocketConfig.dark_lux: conf += 15
            else:
                conf += 15

            if has_gravity:
                gx, gy, gz = self.gravity
                is_flat = gz > 7.5 and abs(gx) < 4.0 and abs(gy) < 4.0
                is_down = gz < -7.0 and abs(gx) < 4.0 and abs(gy) < 4.0
                is_vert = abs(gy) > 5.0

                if is_flat: conf -= 50
                elif is_down or is_vert: conf += 30
            else:
                conf += 20
        
        conf = max(0, min(100, conf))

        if self.state == State.OUT:
            if conf >= PocketConfig.enter_threshold:
                self.state = State.DETECTING
                self.timer = self.time_ms + PocketConfig.enter_debounce
        elif self.state == State.DETECTING:
            if conf < PocketConfig.enter_threshold:
                self.state = State.OUT
            elif self.time_ms >= self.timer:
                self.state = State.IN
                self.in_pocket = True
        elif self.state == State.IN:
            if conf <= PocketConfig.exit_threshold:
                self.state = State.REMOVING
                self.timer = self.time_ms + PocketConfig.exit_debounce
        elif self.state == State.REMOVING:
            if conf > PocketConfig.exit_threshold:
                self.state = State.IN
            elif self.time_ms >= self.timer:
                self.state = State.OUT
                self.in_pocket = False
                
        return self.in_pocket

if __name__ == "__main__":
    # Evaluation
    sim = Simulator()

    # 1. Dark Room, Lying flat, Hand Waves over sensor for 300ms
    print("Scenario 1 (Table Hand Wave):", sim.tick(prox_near=True, lux=1.0, accel=[0,0,9.8], dt_ms=300)) # Expected False

    # 2. Dropped into bright pocket vertically
    for _ in range(6): sim.tick(prox_near=True, lux=50, accel=[0,9.8,0], dt_ms=100)
    print("Scenario 2 (Bright Pocket):", sim.in_pocket) # Expected True

    # 3. Taking it out
    for _ in range(3): sim.tick(prox_near=False, lux=500, accel=[0,4.0,5.0], dt_ms=100)
    print("Scenario 3 (Taken Out):", sim.in_pocket) # Expected False

    # 4. Face down on table
    sim = Simulator()
    for _ in range(6): sim.tick(prox_near=True, lux=0.5, accel=[0,0,-9.8], dt_ms=100)
    print("Scenario 4 (Face Down Table):", sim.in_pocket) # Expected True
