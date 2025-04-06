package fr.domotique.data;

import fr.domotique.base.data.*;
import lombok.*;

import java.time.*;

/// A code that can be used to invite users to the system.
@Getter
@Setter
@AllArgsConstructor
public class InviteCode {
    /// The unique identifier of the invite code.
    String id;
    /// The number of times this code can still be used.
    int usagesLeft;
    /// The role that will be assigned to users who register with this code.
    Role role;
    /// The user ID of the user who created this code.
    Integer creatorId;
    /// The time at which this code was created.
    Instant createdAt;

    public static final EntityInfo<InviteCode> ENTITY = new EntityInfo<>(
        InviteCode.class,
        (r, s) -> new InviteCode(
            r.getString(s.next()),
            r.getInteger(s.next()),
            Role.fromByte(r.get(Byte.class, s.next())),
            r.getInteger(s.next()),
            r.getLocalDateTime(s.next()).toInstant(ZoneOffset.UTC)
        ),
        new EntityColumn<>("id", InviteCode::getId, ColumnType.MANUAL_KEY),
        new EntityColumn<>("usagesLeft", InviteCode::getUsagesLeft),
        new EntityColumn<>("role", InviteCode::getRole),
        new EntityColumn<>("creatorId", InviteCode::getCreatorId),
        new EntityColumn<>("createdAt", InviteCode::createdAtToDB)
    );

    private LocalDateTime createdAtToDB() {
        return LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC);
    }
}
